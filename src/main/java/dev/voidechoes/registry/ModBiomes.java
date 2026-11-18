package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

public final class ModBiomes {
    public static final RegistryKey<Biome> VOID_GROVE = key("void_grove");
    public static final RegistryKey<Biome> CRYSTAL_SPIRES = key("crystal_spires");
    public static final RegistryKey<Biome> ASHEN_REACH = key("ashen_reach");

    private static RegistryKey<Biome> key(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, VoidEchoes.id(name));
    }

    public static final RegistryKey<PlacedFeature> ENDERITE_ORE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, VoidEchoes.id("ore_enderite"));

    public static void init() {
        // Enderite also hides inside the vanilla outer-End islands (the mod's own biomes list it in their JSON).
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.END_HIGHLANDS, BiomeKeys.END_MIDLANDS, BiomeKeys.END_BARRENS),
                GenerationStep.Feature.UNDERGROUND_ORES, ENDERITE_ORE);
        // Outer-End islands. Vanilla highlands carry weight 10 for comparison.
        TheEndBiomes.addHighlandsBiome(VOID_GROVE, 9.0);
        TheEndBiomes.addHighlandsBiome(CRYSTAL_SPIRES, 7.0);
        TheEndBiomes.addHighlandsBiome(ASHEN_REACH, 7.0);
        TheEndBiomes.addMidlandsBiome(VOID_GROVE, VOID_GROVE, 10.0);
        TheEndBiomes.addMidlandsBiome(CRYSTAL_SPIRES, CRYSTAL_SPIRES, 10.0);
        TheEndBiomes.addMidlandsBiome(ASHEN_REACH, ASHEN_REACH, 10.0);
    }
}
