package dev.voidechoes.screen;

import dev.voidechoes.VoidEchoes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreens {
    public static final ScreenHandlerType<VoidWorkbenchScreenHandler> VOID_WORKBENCH = Registry.register(
            Registries.SCREEN_HANDLER, VoidEchoes.id("void_workbench"),
            new ScreenHandlerType<>(VoidWorkbenchScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static void init() {}
}
