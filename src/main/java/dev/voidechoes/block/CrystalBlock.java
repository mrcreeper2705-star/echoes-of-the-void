package dev.voidechoes.block;

import com.mojang.serialization.MapCodec;
import dev.voidechoes.registry.ModParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Translucent, self-lit crystal that glints and chimes. */
public class CrystalBlock extends TransparentBlock {
    public static final MapCodec<CrystalBlock> CODEC = createCodec(CrystalBlock::new);

    public CrystalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(4) == 0) {
            world.addParticle(ModParticles.CRYSTAL_GLINT,
                    pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    0, 0, 0);
        }
        if (random.nextInt(200) == 0) {
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.BLOCKS, 0.6f, 0.6f + random.nextFloat() * 0.6f, false);
        }
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            BlockPos pos = hit.getBlockPos();
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1.0f, 0.5f + world.random.nextFloat());
        }
    }
}
