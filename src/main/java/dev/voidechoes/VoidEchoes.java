package dev.voidechoes;

import dev.voidechoes.registry.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidEchoes implements ModInitializer {
    public static final String MOD_ID = "voidechoes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModParticles.init();
        ModEffects.init();
        ModFluids.init();
        ModBlocks.init();
        ModArmorMaterials.init();
        ModEntities.init();
        ModItems.init();
        ModItemGroup.init();
        ModRecipes.init();
        dev.voidechoes.screen.ModScreens.init();
        ModFeatures.init();
        ModBiomes.init();
        dev.voidechoes.item.EnderiteSetBonus.register();
        dev.voidechoes.fluid.VoidOozeBlock.registerPlayerCheck();
        dev.voidechoes.storm.VoidStorm.register();
        dev.voidechoes.item.AetherWingsItem.registerNetworking();
        LOGGER.info("Echoes of the Void: the End is listening.");
    }
}
