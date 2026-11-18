package dev.voidechoes.client;

import dev.voidechoes.registry.ModBlocks;
import dev.voidechoes.registry.ModEntities;
import dev.voidechoes.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import dev.voidechoes.VoidEchoes;
import dev.voidechoes.registry.ModFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import dev.voidechoes.registry.ModItems;
import dev.voidechoes.screen.ModScreens;

public class VoidEchoesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.GLOWBELL, ModBlocks.VOID_FERN, ModBlocks.ASH_GRASS);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AETHERWOOD_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RESONANT_CRYSTAL, RenderLayer.getTranslucent());

        EntityModelLayerRegistry.registerModelLayer(VoidWispModel.LAYER, VoidWispModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.VOID_WISP, VoidWispRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.VOID_MOTE, VoidMoteParticle.MoteFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CRYSTAL_GLINT, VoidMoteParticle.GlintFactory::new);

        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.VOID_OOZE, ModFluids.FLOWING_VOID_OOZE,
                new SimpleFluidRenderHandler(VoidEchoes.id("block/void_ooze_still"), VoidEchoes.id("block/void_ooze_flow")));
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ModFluids.VOID_OOZE, ModFluids.FLOWING_VOID_OOZE);

        VoidPillarRenderer.register();
        AetherWingsClient.register();
        // The Enderite Ingot reuses the vanilla ingot shape, tinted deep violet (only layer 0).
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex == 0 ? 0xFF8A55E0 : -1, ModItems.ENDERITE_INGOT);

        HandledScreens.register(ModScreens.VOID_WORKBENCH, VoidWorkbenchScreen::new);

        ModelLoadingPlugin.register(ctx -> ctx.addModels(VoidcrystalSwordRenderer.INNER_MODEL));
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.VOIDCRYSTAL_SWORD, new VoidcrystalSwordRenderer());
    }
}
