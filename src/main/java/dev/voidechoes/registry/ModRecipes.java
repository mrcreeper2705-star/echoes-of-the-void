package dev.voidechoes.registry;

import com.mojang.serialization.MapCodec;
import dev.voidechoes.VoidEchoes;
import dev.voidechoes.recipe.VoidCraftingRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModRecipes {
    public static final RecipeType<VoidCraftingRecipe> VOID_CRAFTING = Registry.register(Registries.RECIPE_TYPE,
            VoidEchoes.id("void_crafting"), new RecipeType<>() {
                @Override public String toString() { return "voidechoes:void_crafting"; }
            });

    public static final RecipeSerializer<VoidCraftingRecipe> VOID_CRAFTING_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
            VoidEchoes.id("void_crafting"), new RecipeSerializer<>() {
                @Override public MapCodec<VoidCraftingRecipe> codec() { return VoidCraftingRecipe.CODEC; }
                @Override public PacketCodec<RegistryByteBuf, VoidCraftingRecipe> packetCodec() { return VoidCraftingRecipe.PACKET_CODEC; }
            });

    public static void init() {}
}
