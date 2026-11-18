package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    public static final SimpleParticleType VOID_MOTE = Registry.register(Registries.PARTICLE_TYPE,
            VoidEchoes.id("void_mote"), FabricParticleTypes.simple());
    public static final SimpleParticleType CRYSTAL_GLINT = Registry.register(Registries.PARTICLE_TYPE,
            VoidEchoes.id("crystal_glint"), FabricParticleTypes.simple());

    public static void init() {}
}
