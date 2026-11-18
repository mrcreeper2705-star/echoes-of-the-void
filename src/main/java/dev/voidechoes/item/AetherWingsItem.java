package dev.voidechoes.item;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.registry.ModParticles;
import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Aether Wings: upgraded elytra. While gliding, press jump to beat the wings: a dash of
 * about five blocks up and five blocks forward along your gaze. Three-second cooldown.
 */
public class AetherWingsItem extends ElytraItem implements FabricElytraItem {
    public static final int FLAP_COOLDOWN = 60;
    public static final double FLAP_LIFT = 0.64;
    public static final double FLAP_THRUST = 0.32;

    public AetherWingsItem(Settings settings) {
        super(settings);
    }

    /** Client -> server: "I pressed jump while gliding". */
    public record FlapPayload() implements CustomPayload {
        public static final CustomPayload.Id<FlapPayload> ID = new CustomPayload.Id<>(VoidEchoes.id("wing_flap"));
        public static final PacketCodec<RegistryByteBuf, FlapPayload> CODEC = PacketCodec.unit(new FlapPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerNetworking() {
        PayloadTypeRegistry.playC2S().register(FlapPayload.ID, FlapPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FlapPayload.ID, (payload, ctx) -> flap(ctx.player()));
    }

    public static boolean canFlap(net.minecraft.entity.player.PlayerEntity player) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        return player.isFallFlying() && chest.getItem() instanceof AetherWingsItem && ElytraItem.isUsable(chest)
                && !player.getItemCooldownManager().isCoolingDown(chest.getItem());
    }

    private static void flap(ServerPlayerEntity player) {
        if (!canFlap(player)) return;
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        Vec3d v = player.getVelocity();
        Vec3d look = player.getRotationVector();
        Vec3d forward = new Vec3d(look.x, 0, look.z);
        forward = forward.lengthSquared() < 1.0e-4 ? Vec3d.ZERO : forward.normalize().multiply(FLAP_THRUST);
        player.setVelocity(v.x + forward.x, Math.max(v.y, 0.0) + FLAP_LIFT, v.z + forward.z);
        player.velocityModified = true;
        player.fallDistance = 0f;
        player.getItemCooldownManager().set(chest.getItem(), FLAP_COOLDOWN);
        chest.damage(1, player, EquipmentSlot.CHEST);
        ServerWorld world = player.getServerWorld();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
                SoundCategory.PLAYERS, 1.2f, 1.25f);
        world.spawnParticles(ModParticles.VOID_MOTE, player.getX(), player.getY() + 0.6, player.getZ(), 30, 1.2, 0.25, 1.2, 0.05);
        world.spawnParticles(ModParticles.CRYSTAL_GLINT, player.getX(), player.getY() + 0.8, player.getZ(), 10, 1.0, 0.3, 1.0, 0.02);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.4, player.getZ(), 20, 0.8, 0.2, 0.8, 0.02);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.voidechoes.aether_wings.tooltip").formatted(Formatting.LIGHT_PURPLE));
    }
}
