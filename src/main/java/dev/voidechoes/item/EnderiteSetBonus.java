package dev.voidechoes.item;

import dev.voidechoes.registry.ModArmorMaterials;
import dev.voidechoes.registry.ModParticles;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

/** Full Enderite set: 15% chance to phase out of a hit, blinking a few blocks away. */
public final class EnderiteSetBonus {
    public static final float CHANCE = 0.15f;

    /** One roll per player per tick, even if the damage hook fires more than once for a hit. */
    private static final java.util.Map<java.util.UUID, long[]> LAST_ROLL = new java.util.HashMap<>();

    private EnderiteSetBonus() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(EnderiteSetBonus::allowDamage);
    }

    public static boolean wearsFullSet(LivingEntity entity) {
        int pieces = 0;
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial().equals(ModArmorMaterials.ENDERITE)) pieces++;
        }
        return pieces == 4;
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof PlayerEntity) || !(entity.getWorld() instanceof ServerWorld world)) return true;
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isIn(DamageTypeTags.IS_FALL)) return true;
        if (!wearsFullSet(entity)) return true;
        long tick = world.getTime();
        long[] last = LAST_ROLL.get(entity.getUuid());
        if (last != null && last[0] == tick) return last[1] == 0;      // same hit: reuse the decision
        boolean dodge = entity.getRandom().nextFloat() < CHANCE;
        LAST_ROLL.put(entity.getUuid(), new long[]{tick, dodge ? 1 : 0});
        if (LAST_ROLL.size() > 256) LAST_ROLL.clear();
        if (!dodge) return true;

        double ox = entity.getX(), oy = entity.getY(), oz = entity.getZ();
        for (int i = 0; i < 16; i++) {
            double x = ox + (entity.getRandom().nextDouble() - 0.5) * 8.0;
            double y = MathHelper.clamp(oy + entity.getRandom().nextInt(5) - 2, world.getBottomY(),
                    world.getBottomY() + world.getLogicalHeight() - 1);
            double z = oz + (entity.getRandom().nextDouble() - 0.5) * 8.0;
            if (entity.teleport(x, y, z, true)) {
                world.spawnParticles(ModParticles.CRYSTAL_GLINT, ox, oy + 1, oz, 12, 0.3, 0.5, 0.3, 0.05);
                world.playSound(null, ox, oy, oz, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8f, 1.6f);
                world.playSound(null, x, y, z, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0f, 1.8f);
                return false;   // dodged
            }
        }
        LAST_ROLL.put(entity.getUuid(), new long[]{tick, 0});    // nowhere to go: take the hit
        return true;
    }
}
