package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
    public static final ItemGroup MAIN = Registry.register(Registries.ITEM_GROUP, VoidEchoes.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.RESONANT_CRYSTAL))
                    .displayName(Text.translatable("itemGroup.voidechoes"))
                    .entries((ctx, entries) -> {
                        entries.add(ModItems.VOIDSTEP_CHARM);
                        entries.add(ModItems.VOIDCRYSTAL_SWORD);
                        entries.add(ModItems.AETHER_WINGS);
                        entries.add(ModItems.VOID_CRYSTAL_SHARD);
                        entries.add(ModItems.VOID_WISP_SPAWN_EGG);
                        entries.add(ModFluids.VOID_OOZE_BUCKET);
                        entries.add(ModItems.ENDERITE_SCRAP);
                        entries.add(ModItems.ENDERITE_INGOT);
                        entries.add(ModItems.ENDERITE_UPGRADE_SMITHING_TEMPLATE);
                        entries.add(ModItems.ENDERITE_SWORD);
                        entries.add(ModItems.ENDERITE_PICKAXE);
                        entries.add(ModItems.ENDERITE_AXE);
                        entries.add(ModItems.ENDERITE_SHOVEL);
                        entries.add(ModItems.ENDERITE_HOE);
                        entries.add(ModItems.ENDERITE_HELMET);
                        entries.add(ModItems.ENDERITE_CHESTPLATE);
                        entries.add(ModItems.ENDERITE_LEGGINGS);
                        entries.add(ModItems.ENDERITE_BOOTS);
                        for (Block b : ModBlocks.WITH_ITEMS) entries.add(b);
                    })
                    .build());

    public static void init() {}
}
