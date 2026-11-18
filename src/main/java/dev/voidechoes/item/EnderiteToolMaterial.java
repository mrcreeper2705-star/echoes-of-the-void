package dev.voidechoes.item;

import dev.voidechoes.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/** One tier above netherite. */
public final class EnderiteToolMaterial implements ToolMaterial {
    public static final EnderiteToolMaterial INSTANCE = new EnderiteToolMaterial();

    @Override public int getDurability() { return 2600; }
    @Override public float getMiningSpeedMultiplier() { return 11.0f; }
    @Override public float getAttackDamage() { return 5.0f; }
    @Override public TagKey<Block> getInverseTag() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantability() { return 18; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(ModItems.ENDERITE_INGOT); }
}
