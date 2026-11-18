package dev.voidechoes.fluid;

import dev.voidechoes.registry.ModEffects;
import dev.voidechoes.registry.ModParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Wading through Stellar Aether is slow, cushions falls and leaves you Weightless for a while. */
public class VoidOozeBlock extends FluidBlock {
    /** Players standing still trigger no block collisions server-side, so check them every tick too. */
    public static void registerPlayerCheck() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
                if (player.isSpectator()) continue;
                if (dev.voidechoes.registry.ModFluids.VOID_OOZE.matchesType(world.getFluidState(player.getBlockPos()).getFluid())
                        || dev.voidechoes.registry.ModFluids.VOID_OOZE.matchesType(world.getFluidState(BlockPos.ofFloored(player.getEyePos())).getFluid())) {
                    applyEffect(player);
                }
            }
        });
    }

    private static void applyEffect(LivingEntity living) {
        living.addStatusEffect(new StatusEffectInstance(ModEffects.WEIGHTLESS, 200, 0, false, true, true));
        living.fallDistance = 0f;
    }

    public VoidOozeBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        entity.slowMovement(state, new Vec3d(0.55, 0.35, 0.55));
        entity.fallDistance = 0f;
        if (!world.isClient && entity instanceof LivingEntity living) applyEffect(living);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(6) == 0 && world.getBlockState(pos.up()).isAir()) {
            world.addParticle(ModParticles.VOID_MOTE, pos.getX() + random.nextDouble(), pos.getY() + 0.95,
                    pos.getZ() + random.nextDouble(), 0, 0.03, 0);
        }
    }
}
