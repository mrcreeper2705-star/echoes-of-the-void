package dev.voidechoes.world;

import dev.voidechoes.registry.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Finds the rim of an island and cuts a short brook of Stellar Aether that runs to the
 * edge and pours over it, falling all the way down into the void.
 */
public class AetherFallFeature extends Feature<DefaultFeatureConfig> {
    private static final int SEARCH = 7;
    private static final int DROP = 10;   // how much empty space must lie beyond the rim

    public AetherFallFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        BlockPos o = ctx.getOrigin();
        for (int attempt = 0; attempt < 48; attempt++) {
            int x = o.getX() + random.nextInt(2 * SEARCH + 1) - SEARCH;
            int z = o.getZ() + random.nextInt(2 * SEARCH + 1) - SEARCH;
            int h = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
            if (h < world.getBottomY() + DROP + 4) continue;
            BlockPos top = new BlockPos(x, h, z);
            if (!world.getBlockState(top).isOpaqueFullCube(world, top)) continue;
            for (Direction out : Direction.Type.HORIZONTAL.getShuffled(random)) {
                if (isRim(world, top, out) && carve(world, top, out, random)) return true;
            }
        }
        return false;
    }

    /** The column just past the rim must be empty for a good way down. */
    private static boolean isRim(StructureWorldAccess world, BlockPos top, Direction out) {
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int k = 0; k <= DROP; k++) {
            p.set(top.getX() + out.getOffsetX(), top.getY() - k, top.getZ() + out.getOffsetZ());
            if (!world.getBlockState(p).isAir()) return false;
        }
        return true;
    }

    private boolean carve(StructureWorldAccess world, BlockPos rim, Direction out, Random random) {
        Direction in = out.getOpposite();
        int length = 2 + random.nextInt(2);
        BlockState source = ModFluids.VOID_OOZE_BLOCK.getDefaultState().with(FluidBlock.LEVEL, 0);
        // The brook must have solid banks and bed, otherwise it would leak somewhere ugly.
        for (int k = 0; k < length; k++) {
            BlockPos c = rim.offset(in, k);
            if (!world.getBlockState(c.down()).isOpaqueFullCube(world, c.down())) return false;
            for (Direction side : new Direction[]{out.rotateYClockwise(), out.rotateYCounterclockwise()}) {
                BlockPos b = c.offset(side);
                if (!world.getBlockState(b).isOpaqueFullCube(world, b)) return false;
            }
        }
        if (!world.getBlockState(rim.offset(in, length)).isOpaqueFullCube(world, rim.offset(in, length))) return false;
        for (int k = 0; k < length; k++) {
            BlockPos c = rim.offset(in, k);
            setBlockState(world, c, source);
            if (world.getBlockState(c.up()).isReplaceable()) setBlockState(world, c.up(), net.minecraft.block.Blocks.AIR.getDefaultState());
        }
        world.scheduleFluidTick(rim, ModFluids.VOID_OOZE, 1);
        return true;
    }
}
