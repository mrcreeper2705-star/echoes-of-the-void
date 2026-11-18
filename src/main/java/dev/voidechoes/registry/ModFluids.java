package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.fluid.VoidOozeBlock;
import dev.voidechoes.fluid.VoidOozeFluid;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModFluids {
    public static final FlowableFluid VOID_OOZE = Registry.register(Registries.FLUID, VoidEchoes.id("void_ooze"), new VoidOozeFluid.Still());
    public static final FlowableFluid FLOWING_VOID_OOZE = Registry.register(Registries.FLUID, VoidEchoes.id("flowing_void_ooze"), new VoidOozeFluid.Flowing());

    public static final Block VOID_OOZE_BLOCK = Registry.register(Registries.BLOCK, VoidEchoes.id("void_ooze"),
            new VoidOozeBlock(VOID_OOZE, AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).replaceable().noCollision()
                    .strength(100f).pistonBehavior(PistonBehavior.DESTROY).dropsNothing().liquid()
                    .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY).instrument(NoteBlockInstrument.BASEDRUM).luminance(s -> 8)));

    public static final Item VOID_OOZE_BUCKET = Registry.register(Registries.ITEM, VoidEchoes.id("void_ooze_bucket"),
            new BucketItem(VOID_OOZE, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));

    public static void init() {}
}
