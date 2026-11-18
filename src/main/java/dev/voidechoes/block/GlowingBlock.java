package dev.voidechoes.block;

import dev.voidechoes.registry.ModParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A solid block that breathes soft void motes out of its exposed faces. */
public class GlowingBlock extends Block {
    public GlowingBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) != 0) return;
        Direction dir = Direction.random(random);
        BlockPos side = pos.offset(dir);
        if (world.getBlockState(side).isOpaqueFullCube(world, side)) return;
        double x = pos.getX() + 0.5 + dir.getOffsetX() * 0.55 + (random.nextDouble() - 0.5) * (dir.getOffsetX() == 0 ? 0.8 : 0);
        double y = pos.getY() + 0.5 + dir.getOffsetY() * 0.55 + (random.nextDouble() - 0.5) * (dir.getOffsetY() == 0 ? 0.8 : 0);
        double z = pos.getZ() + 0.5 + dir.getOffsetZ() * 0.55 + (random.nextDouble() - 0.5) * (dir.getOffsetZ() == 0 ? 0.8 : 0);
        world.addParticle(ModParticles.VOID_MOTE, x, y, z, dir.getOffsetX() * 0.02, 0.02, dir.getOffsetZ() * 0.02);
    }
}
