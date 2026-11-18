package dev.voidechoes.storm;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Void Storm: rare End weather. While it lasts, Void Pillars (black, white-rimmed columns
 * 5 blocks wide, from the void to the sky) flash into being over open void and fade within
 * a second, with thunder. Touching one blinds and withers you for 3 seconds.
 */
public final class VoidStorm {
    public static final double RADIUS = 2.5;
    public static final int LIFETIME = 20;               // ticks a pillar exists
    /** Vanilla lightning: 1 in 100000 per loaded chunk per tick. Pillars strike twice as often. */
    private static final int CHANCE_PER_CHUNK = 50000;
    private static final int RANGE = 128;                 // blocks around each player where pillars may form

    private static final List<Pillar> ACTIVE = new ArrayList<>();
    private static int strikes, attempts;

    private record Pillar(ServerWorld world, double x, double z, int[] age) {}

    private VoidStorm() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(PillarPayload.ID, PillarPayload.CODEC);
        ServerTickEvents.END_WORLD_TICK.register(VoidStorm::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
                CommandManager.literal("voidstorm").requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("start")
                                .executes(c -> start(c.getSource().getServer().getWorld(World.END), 6000, c.getSource()))
                                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1, 100000))
                                        .executes(c -> start(c.getSource().getServer().getWorld(World.END),
                                                IntegerArgumentType.getInteger(c, "seconds") * 20, c.getSource()))))
                        .then(CommandManager.literal("stop").executes(c -> {
                            VoidStormState s = state(c.getSource().getServer().getWorld(World.END));
                            s.stormTicks = 0;
                            s.clearTicks = rollClear(c.getSource().getWorld().getRandom());
                            s.markDirty();
                            c.getSource().sendFeedback(() -> Text.translatable("commands.voidechoes.voidstorm.stop"), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("status").executes(c -> {
                            VoidStormState s = state(c.getSource().getServer().getWorld(World.END));
                            c.getSource().sendFeedback(() -> Text.literal("storm=" + s.stormTicks + " clear=" + s.clearTicks
                                    + " attempts=" + attempts + " strikes=" + strikes), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("strike")
                                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("z", DoubleArgumentType.doubleArg()).executes(c -> {
                                            spawn(c.getSource().getServer().getWorld(World.END),
                                                    DoubleArgumentType.getDouble(c, "x"), DoubleArgumentType.getDouble(c, "z"));
                                            return 1;
                                        }))))));
    }

    private static int start(ServerWorld end, int ticks, net.minecraft.server.command.ServerCommandSource src) {
        VoidStormState s = state(end);
        s.stormTicks = ticks;
        s.markDirty();
        src.sendFeedback(() -> Text.translatable("commands.voidechoes.voidstorm.start"), true);
        return 1;
    }

    public static VoidStormState state(ServerWorld end) {
        return end.getPersistentStateManager().getOrCreate(VoidStormState.TYPE, "voidechoes_void_storm");
    }

    /** Same spread as vanilla thunder: 0.5 to 7.5 days of calm, 3 to 13 minutes of storm. */
    private static int rollClear(Random r) { return 12000 + r.nextInt(168000); }
    private static int rollStorm(Random r) { return 3600 + r.nextInt(12000); }

    public static boolean isStorming(ServerWorld world) {
        return world.getRegistryKey() == World.END && state(world).stormTicks > 0;
    }

    private static void tick(ServerWorld world) {
        tickPillars(world);
        if (world.getRegistryKey() != World.END) return;
        VoidStormState s = state(world);
        Random random = world.getRandom();
        if (s.stormTicks > 0) {
            s.stormTicks--;
            if (s.stormTicks == 0) s.clearTicks = rollClear(random);
            s.markDirty();
        } else {
            if (s.clearTicks < 0) s.clearTicks = rollClear(random);
            if (--s.clearTicks <= 0) s.stormTicks = rollStorm(random);
            s.markDirty();
            return;
        }
        // Roughly the chunks within a player's view: (2*8+1)^2 = 289 of them.
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (random.nextInt(CHANCE_PER_CHUNK) >= 289) continue;
            double x = player.getX() + (random.nextDouble() * 2 - 1) * RANGE;
            double z = player.getZ() + (random.nextDouble() * 2 - 1) * RANGE;
            attempts++;
            if (isOpenSky(world, x, z)) {
                strikes++;
                spawn(world, x, z);
            }
        }
    }

    /** Only where the whole 5-block-wide column, from the void to the build limit, is empty. */
    public static boolean isOpenSky(ServerWorld world, double cx, double cz) {
        int r = (int) Math.ceil(RADIUS);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > (RADIUS + 0.5) * (RADIUS + 0.5)) continue;
                int x = (int) Math.floor(cx) + dx, z = (int) Math.floor(cz) + dz;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) return false;
                if (world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) > world.getBottomY()) return false;
            }
        }
        return true;
    }

    public static void spawn(ServerWorld world, double x, double z) {
        ACTIVE.add(new Pillar(world, x, z, new int[]{0}));
        double y = world.getBottomY() + 70;
        world.playSound(null, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER,
                10000.0f, 0.55f + world.getRandom().nextFloat() * 0.2f);
        world.playSound(null, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER,
                2.0f, 0.5f + world.getRandom().nextFloat() * 0.2f);
        PillarPayload payload = new PillarPayload(x, z);
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.squaredDistanceTo(x, p.getY(), z) < 512 * 512) ServerPlayNetworking.send(p, payload);
        }
    }

    private static void tickPillars(ServerWorld world) {
        for (Iterator<Pillar> it = ACTIVE.iterator(); it.hasNext(); ) {
            Pillar pillar = it.next();
            if (pillar.world() != world) continue;
            if (++pillar.age()[0] > LIFETIME) {
                it.remove();
                continue;
            }
            for (ServerPlayerEntity p : world.getPlayers()) {
                if (p.isSpectator() || p.isCreative()) continue;
                double dx = p.getX() - pillar.x(), dz = p.getZ() - pillar.z();
                double reach = RADIUS + p.getWidth() / 2;
                if (dx * dx + dz * dz <= reach * reach) {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
                }
            }
        }
    }
}
