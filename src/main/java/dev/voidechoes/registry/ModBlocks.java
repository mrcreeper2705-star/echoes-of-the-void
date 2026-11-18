package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.block.CrystalBlock;
import dev.voidechoes.block.GlowingBlock;
import dev.voidechoes.block.VoidPlantBlock;
import dev.voidechoes.block.VoidWorkbenchBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {
    /** Every block that also gets a BlockItem, in creative-tab order. */
    public static final List<Block> WITH_ITEMS = new ArrayList<>();

    public static final Block VOID_MOSS = register("void_moss", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
            .strength(3.0f, 9.0f).sounds(BlockSoundGroup.NYLIUM).luminance(s -> 2)));

    public static final Block VOIDSTONE = register("voidstone", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
            .strength(3.0f, 9.0f).sounds(BlockSoundGroup.DEEPSLATE)));

    public static final Block VOIDSTONE_BRICKS = register("voidstone_bricks", new Block(AbstractBlock.Settings.copy(VOIDSTONE)
            .sounds(BlockSoundGroup.DEEPSLATE_BRICKS)));

    public static final Block CHISELED_VOIDSTONE = register("chiseled_voidstone", new GlowingBlock(AbstractBlock.Settings.copy(VOIDSTONE)
            .sounds(BlockSoundGroup.DEEPSLATE_BRICKS).luminance(s -> 11).emissiveLighting((s, w, p) -> true)));

    public static final Block ASHEN_SAND = register("ashen_sand", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.PALE_YELLOW).instrument(NoteBlockInstrument.SNARE)
            .strength(0.6f).sounds(BlockSoundGroup.SAND)));

    public static final Block AETHERWOOD_LOG = register("aetherwood_log", new PillarBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_PURPLE).instrument(NoteBlockInstrument.BASS)
            .strength(2.0f).sounds(BlockSoundGroup.CHERRY_WOOD)));

    public static final Block AETHERWOOD_PLANKS = register("aetherwood_planks", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_PURPLE).instrument(NoteBlockInstrument.BASS)
            .strength(2.0f, 3.0f).sounds(BlockSoundGroup.CHERRY_WOOD)));

    public static final Block AETHERWOOD_LEAVES = register("aetherwood_leaves", new LeavesBlock(AbstractBlock.Settings.copy(Blocks.CHERRY_LEAVES)
            .mapColor(MapColor.CYAN).luminance(s -> 7)));

    public static final Block RESONANT_CRYSTAL = register("resonant_crystal", new CrystalBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_BLUE).strength(1.5f).requiresTool().nonOpaque()
            .sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(s -> 13).emissiveLighting((s, w, p) -> true)
            .allowsSpawning((s, w, p, t) -> false).solidBlock((s, w, p) -> false)
            .suffocates((s, w, p) -> false).blockVision((s, w, p) -> false)));

    public static final Block VOID_WORKBENCH = register("void_workbench", new VoidWorkbenchBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
            .strength(4.0f, 12.0f).sounds(BlockSoundGroup.DEEPSLATE_BRICKS).luminance(s -> 6)));

    public static final Block ENDERITE_ORE = register("enderite_ore", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresTool()
            .strength(30.0f, 1200.0f).sounds(BlockSoundGroup.ANCIENT_DEBRIS).luminance(s -> 3)),
            new Item.Settings().rarity(net.minecraft.util.Rarity.EPIC).fireproof());

    public static final Block GLOWBELL = register("glowbell", new VoidPlantBlock(plant().luminance(s -> 12).emissiveLighting((s, w, p) -> true), true));
    public static final Block VOID_FERN = register("void_fern", new VoidPlantBlock(plant().luminance(s -> 3), false));
    public static final Block ASH_GRASS = register("ash_grass", new VoidPlantBlock(plant().mapColor(MapColor.PALE_YELLOW), false));

    private static AbstractBlock.Settings plant() {
        return AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).replaceable().noCollision().breakInstantly()
                .sounds(BlockSoundGroup.NETHER_SPROUTS).offset(AbstractBlock.OffsetType.XZ)
                .pistonBehavior(PistonBehavior.DESTROY);
    }

    private static Block register(String name, Block block) {
        return register(name, block, new Item.Settings());
    }

    private static Block register(String name, Block block, Item.Settings itemSettings) {
        Registry.register(Registries.BLOCK, VoidEchoes.id(name), block);
        Registry.register(Registries.ITEM, VoidEchoes.id(name), new BlockItem(block, itemSettings));
        WITH_ITEMS.add(block);
        return block;
    }

    public static void init() {}
}
