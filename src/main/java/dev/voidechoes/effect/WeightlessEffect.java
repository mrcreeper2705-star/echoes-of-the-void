package dev.voidechoes.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Lower gravity, higher jumps, no fall damage. Attribute modifiers are added on registration. */
public class WeightlessEffect extends StatusEffect {
    public WeightlessEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xB07CF0);
    }
}
