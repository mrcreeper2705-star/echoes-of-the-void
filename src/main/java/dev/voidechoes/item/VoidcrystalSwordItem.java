package dev.voidechoes.item;

import dev.voidechoes.registry.ModParticles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Right-click: instant ender-pearl-style teleport to the block you look at
 * (3 durability, 1.5 damage, pearl cooldown).
 * Every third hit against the same target "echoes": a burst of void energy
 * deals bonus damage and briefly slows everything nearby.
 */
public class VoidcrystalSwordItem extends SwordItem {
    public static final double BLINK_RANGE = 24.0;
    public static final int BLINK_COOLDOWN = 20; // same as an ender pearl
    private static final int BLINK_DURABILITY = 3;
    private static final float BLINK_DAMAGE = 1.5f;

    public VoidcrystalSwordItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld server) {
            Vec3d target = VoidBlink.findDestination(server, user, BLINK_RANGE, true);
            if (target == null) {
                VoidBlink.fail(server, user);
                return TypedActionResult.fail(stack);
            }
            VoidBlink.blink(server, user, target);
            user.damage(server.getDamageSources().fall(), BLINK_DAMAGE);
            stack.damage(BLINK_DURABILITY, user, LivingEntity.getSlotForHand(hand));
            user.getItemCooldownManager().set(this, BLINK_COOLDOWN);
        }
        user.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ModParticles.CRYSTAL_GLINT, target.getX(), target.getBodyY(0.5), target.getZ(),
                    6, 0.3, 0.4, 0.3, 0.05);
            int echo = target.getCommandTags().contains("voidechoes.echo2") ? 3
                    : target.getCommandTags().contains("voidechoes.echo1") ? 2 : 1;
            target.removeCommandTag("voidechoes.echo1");
            target.removeCommandTag("voidechoes.echo2");
            if (echo < 3) {
                target.addCommandTag("voidechoes.echo" + echo);
            } else {
                world.spawnParticles(ModParticles.VOID_MOTE, target.getX(), target.getBodyY(0.5), target.getZ(),
                        40, 1.2, 0.6, 1.2, 0.08);
                world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
                        SoundCategory.PLAYERS, 1.2f, 0.6f);
                for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(3.0),
                        e -> e != attacker && e.isAlive())) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1), attacker);
                    e.damage(world.getDamageSources().indirectMagic(attacker, attacker), e == target ? 4.0f : 2.5f);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.voidechoes.voidcrystal_sword.tooltip.blink").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.translatable("item.voidechoes.voidcrystal_sword.tooltip").formatted(Formatting.DARK_AQUA));
    }
}
