package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.world.AetherFallFeature;
import dev.voidechoes.world.CrystalSpikeFeature;
import dev.voidechoes.world.SurfaceCoverFeature;
import dev.voidechoes.world.VoidShrineFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.Feature;

public final class ModFeatures {
    public static final Feature<SurfaceCoverFeature.Config> SURFACE_COVER =
            Registry.register(Registries.FEATURE, VoidEchoes.id("surface_cover"), new SurfaceCoverFeature());
    public static final Feature<CrystalSpikeFeature.Config> CRYSTAL_SPIKE =
            Registry.register(Registries.FEATURE, VoidEchoes.id("crystal_spike"), new CrystalSpikeFeature());
    public static final VoidShrineFeature VOID_SHRINE =
            Registry.register(Registries.FEATURE, VoidEchoes.id("void_shrine"), new VoidShrineFeature());

    public static final AetherFallFeature AETHER_FALL =
            Registry.register(Registries.FEATURE, VoidEchoes.id("aether_fall"), new AetherFallFeature());

    public static void init() {}
}
