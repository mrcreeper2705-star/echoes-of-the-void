package dev.voidechoes.block;

import com.mojang.serialization.MapCodec;
import dev.voidechoes.registry.ModParticles;
import dev.voidechoes.screen.VoidWorkbenchScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** The Void Workbench: a 5x5 crafting table for the mod's strongest gear. */
public class VoidWorkbenchBlock extends Block {
    public static final MapCodec<VoidWorkbenchBlock> CODEC = createCodec(VoidWorkbenchBlock::new);
    private static final Text TITLE = Text.translatable("container.voidechoes.void_workbench");

    public VoidWorkbenchBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.CONSUME;
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inv, player) ->
                new VoidWorkbenchScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos)), TITLE);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) {
            world.addParticle(ModParticles.VOID_MOTE, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.02, 0);
        }
    }
}
