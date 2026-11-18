package dev.voidechoes.fluid;

import dev.voidechoes.registry.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/** Void Ooze: a thick, slow, glowing purple liquid. Not infinite, flows only a short way. */
public abstract class VoidOozeFluid extends FlowableFluid {
    @Override public Fluid getFlowing() { return ModFluids.FLOWING_VOID_OOZE; }
    @Override public Fluid getStill() { return ModFluids.VOID_OOZE; }
    @Override public Item getBucketItem() { return ModFluids.VOID_OOZE_BUCKET; }
    @Override protected boolean isInfinite(World world) { return false; }
    @Override protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        net.minecraft.block.Block.dropStacks(state, world, pos, state.hasBlockEntity() ? world.getBlockEntity(pos) : null);
    }
    @Override protected int getMaxFlowDistance(WorldView world) { return 3; }
    @Override protected int getLevelDecreasePerBlock(WorldView world) { return 2; }
    @Override public int getTickRate(WorldView world) { return 10; }
    @Override protected float getBlastResistance() { return 100f; }
    @Override public boolean matchesType(Fluid fluid) { return fluid == getStill() || fluid == getFlowing(); }
    @Override protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !matchesType(fluid);
    }
    @Override protected BlockState toBlockState(FluidState state) {
        return ModFluids.VOID_OOZE_BLOCK.getDefaultState().with(net.minecraft.block.FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static class Still extends VoidOozeFluid {
        @Override public int getLevel(FluidState state) { return 8; }
        @Override public boolean isStill(FluidState state) { return true; }
    }

    public static class Flowing extends VoidOozeFluid {
        @Override protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
        @Override public int getLevel(FluidState state) { return state.get(LEVEL); }
        @Override public boolean isStill(FluidState state) { return false; }
    }
}
