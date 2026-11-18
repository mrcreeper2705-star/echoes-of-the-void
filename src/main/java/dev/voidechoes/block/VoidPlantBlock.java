package dev.voidechoes.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.voidechoes.registry.ModBlocks;
import dev.voidechoes.registry.ModParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Small End flora that roots on end stone and the mod's surface blocks. */
public class VoidPlantBlock extends PlantBlock {
    public static final MapCodec<VoidPlantBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            createSettingsCodec(),
            com.mojang.serialization.Codec.BOOL.fieldOf("emits_motes").forGetter(b -> b.emitsMotes)
    ).apply(i, VoidPlantBlock::new));
    private static final VoxelShape SHAPE = createCuboidShape(2, 0, 2, 14, 13, 14);

    private final boolean emitsMotes;

    public VoidPlantBlock(Settings settings, boolean emitsMotes) {
        super(settings);
        this.emitsMotes = emitsMotes;
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d off = state.getModelOffset(world, pos);
        return SHAPE.offset(off.x, off.y, off.z);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.END_STONE) || floor.isOf(ModBlocks.VOID_MOSS) || floor.isOf(ModBlocks.ASHEN_SAND)
                || floor.isOf(ModBlocks.VOIDSTONE) || super.canPlantOnTop(floor, world, pos);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (emitsMotes && random.nextInt(4) == 0) {
            world.addParticle(ModParticles.VOID_MOTE, pos.getX() + 0.3 + random.nextDouble() * 0.4,
                    pos.getY() + 0.5 + random.nextDouble() * 0.3, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0, 0.03, 0);
        }
    }
}
