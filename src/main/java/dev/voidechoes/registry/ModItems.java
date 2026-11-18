package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.item.EnderiteToolMaterial;
import dev.voidechoes.item.VoidcrystalMaterial;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.List;
import dev.voidechoes.item.VoidcrystalSwordItem;
import dev.voidechoes.item.VoidstepCharmItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
    public static final Item VOID_CRYSTAL_SHARD = register("void_crystal_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    public static final Item VOIDSTEP_CHARM = register("voidstep_charm", new VoidstepCharmItem(new Item.Settings()
            .maxDamage(160).rarity(Rarity.RARE)));

    public static final Item VOIDCRYSTAL_SWORD = register("voidcrystal_sword", new VoidcrystalSwordItem(VoidcrystalMaterial.INSTANCE,
            new Item.Settings().rarity(Rarity.RARE)
                    .attributeModifiers(SwordItem.createAttributeModifiers(VoidcrystalMaterial.INSTANCE, 3, -2.2f))));

    public static final Item VOID_WISP_SPAWN_EGG = register("void_wisp_spawn_egg",
            new SpawnEggItem(ModEntities.VOID_WISP, 0x1a0b2e, 0x6ff3ff, new Item.Settings()));

    // ---------------------------------------------------------------- Enderite
    private static Item.Settings legendary() {
        return new Item.Settings().rarity(Rarity.EPIC).fireproof();
    }

    public static final Item ENDERITE_SCRAP = register("enderite_scrap", new Item(legendary()));
    public static final Item ENDERITE_INGOT = register("enderite_ingot", new Item(legendary()));

    public static final Item ENDERITE_UPGRADE_SMITHING_TEMPLATE = register("enderite_upgrade_smithing_template",
            new SmithingTemplateItem(
                    Text.translatable("item.voidechoes.smithing_template.enderite_upgrade.applies_to").formatted(Formatting.BLUE),
                    Text.translatable("item.voidechoes.smithing_template.enderite_upgrade.ingredients").formatted(Formatting.BLUE),
                    Text.translatable("upgrade.voidechoes.enderite_upgrade").formatted(Formatting.GRAY),
                    Text.translatable("item.voidechoes.smithing_template.enderite_upgrade.base_slot_description"),
                    Text.translatable("item.voidechoes.smithing_template.enderite_upgrade.additions_slot_description"),
                    List.of(Identifier.ofVanilla("item/empty_armor_slot_helmet"), Identifier.ofVanilla("item/empty_slot_sword"),
                            Identifier.ofVanilla("item/empty_armor_slot_chestplate"), Identifier.ofVanilla("item/empty_slot_pickaxe"),
                            Identifier.ofVanilla("item/empty_armor_slot_leggings"), Identifier.ofVanilla("item/empty_slot_axe"),
                            Identifier.ofVanilla("item/empty_armor_slot_boots"), Identifier.ofVanilla("item/empty_slot_hoe"),
                            Identifier.ofVanilla("item/empty_slot_shovel")),
                    List.of(Identifier.ofVanilla("item/empty_slot_ingot"))));

    private static final EnderiteToolMaterial EM = EnderiteToolMaterial.INSTANCE;
    public static final Item ENDERITE_SWORD = register("enderite_sword", new SwordItem(EM,
            legendary().attributeModifiers(SwordItem.createAttributeModifiers(EM, 3, -2.4f))));
    public static final Item ENDERITE_PICKAXE = register("enderite_pickaxe", new PickaxeItem(EM,
            legendary().attributeModifiers(PickaxeItem.createAttributeModifiers(EM, 1.0f, -2.8f))));
    public static final Item ENDERITE_AXE = register("enderite_axe", new AxeItem(EM,
            legendary().attributeModifiers(AxeItem.createAttributeModifiers(EM, 5.0f, -3.0f))));
    public static final Item ENDERITE_SHOVEL = register("enderite_shovel", new ShovelItem(EM,
            legendary().attributeModifiers(ShovelItem.createAttributeModifiers(EM, 1.5f, -3.0f))));
    public static final Item ENDERITE_HOE = register("enderite_hoe", new HoeItem(EM,
            legendary().attributeModifiers(HoeItem.createAttributeModifiers(EM, -5.0f, 0.0f))));

    public static final Item ENDERITE_HELMET = armor("enderite_helmet", ArmorItem.Type.HELMET);
    public static final Item ENDERITE_CHESTPLATE = armor("enderite_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final Item ENDERITE_LEGGINGS = armor("enderite_leggings", ArmorItem.Type.LEGGINGS);
    public static final Item ENDERITE_BOOTS = armor("enderite_boots", ArmorItem.Type.BOOTS);

    public static final Item AETHER_WINGS = register("aether_wings", new dev.voidechoes.item.AetherWingsItem(
            new Item.Settings().maxDamage(864).rarity(Rarity.EPIC).fireproof()));

    private static Item armor(String name, ArmorItem.Type type) {
        return register(name, new ArmorItem(ModArmorMaterials.ENDERITE, type, legendary().maxDamage(type.getMaxDamage(45))));
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, VoidEchoes.id(name), item);
    }

    public static void init() {}
}
