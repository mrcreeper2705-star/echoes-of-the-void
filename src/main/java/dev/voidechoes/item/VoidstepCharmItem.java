package dev.voidechoes.item;

import dev.voidechoes.registry.ModParticles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/** Artifact: blink up to 10 blocks in the direction you are looking. */
public class VoidstepCharmItem extends Item {
    private static final double RANGE = 10.0;
    private static final int COOLDOWN = 50;

    public VoidstepCharmItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld server) {
            Vec3d target = VoidBlink.findDestination(server, user, RANGE, false);
            if (target == null) {
                VoidBlink.fail(server, user);
                user.getItemCooldownManager().set(this, 10);
                return TypedActionResult.fail(stack);
            }
            VoidBlink.blink(server, user, target);
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        user.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.voidechoes.voidstep_charm.tooltip").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.translatable("item.voidechoes.artifact").formatted(Formatting.GOLD, Formatting.ITALIC));
    }
}
