package dev.voidechoes.mixin;

import dev.voidechoes.VoidEchoes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Items tagged voidechoes:void_proof refuse to be lost in the void: once they drop below
 * the world they rise back up, glowing, and hover at the height of the End islands.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    private static final TagKey<Item> VOID_PROOF = TagKey.of(RegistryKeys.ITEM, VoidEchoes.id("void_proof"));

    @Shadow public abstract ItemStack getStack();

    protected ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void voidechoes$riseFromVoid(CallbackInfo ci) {
        if (!this.getStack().isIn(VOID_PROOF)) return;
        int bottom = this.getWorld().getBottomY();
        if (this.getY() < bottom) {
            this.setNoGravity(true);
            this.setGlowing(true);
        }
        if (this.hasNoGravity() && this.isGlowing()) {
            double target = bottom + 64 + 2;   // hover around the islands' height
            Vec3d v = this.getVelocity();
            double vy = this.getY() < target ? Math.min(0.6, (target - this.getY()) * 0.05 + 0.05) : 0.0;
            this.setVelocity(v.x * 0.9, vy, v.z * 0.9);
        }
    }
}
