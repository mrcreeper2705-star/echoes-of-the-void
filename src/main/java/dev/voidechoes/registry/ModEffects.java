package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.effect.WeightlessEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModEffects {
    public static final RegistryEntry<StatusEffect> WEIGHTLESS = Registry.registerReference(Registries.STATUS_EFFECT,
            VoidEchoes.id("weightless"), new WeightlessEffect()
                    .addAttributeModifier(EntityAttributes.GENERIC_GRAVITY, VoidEchoes.id("effect.weightless.gravity"),
                            -0.6, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_JUMP_STRENGTH, VoidEchoes.id("effect.weightless.jump"),
                            0.35, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, VoidEchoes.id("effect.weightless.fall"),
                            40.0, EntityAttributeModifier.Operation.ADD_VALUE));

    public static void init() {}
}
