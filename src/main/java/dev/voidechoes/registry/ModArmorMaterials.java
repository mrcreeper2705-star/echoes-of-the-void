package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> ENDERITE = Registry.registerReference(Registries.ARMOR_MATERIAL,
            VoidEchoes.id("enderite"), new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                        m.put(ArmorItem.Type.BOOTS, 4);
                        m.put(ArmorItem.Type.LEGGINGS, 7);
                        m.put(ArmorItem.Type.CHESTPLATE, 9);
                        m.put(ArmorItem.Type.HELMET, 4);
                        m.put(ArmorItem.Type.BODY, 12);
                    }),
                    18, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, () -> Ingredient.ofItems(ModItems.ENDERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(VoidEchoes.id("enderite"))), 4.0f, 0.15f));

    public static void init() {}
}
