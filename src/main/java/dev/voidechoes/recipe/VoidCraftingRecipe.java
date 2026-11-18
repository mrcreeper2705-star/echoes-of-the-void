package dev.voidechoes.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.voidechoes.registry.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A shaped recipe for the 5x5 Void Workbench. Placement in the grid is free; mirrored shapes also match. */
public class VoidCraftingRecipe implements Recipe<CraftingRecipeInput> {
    public static final int MAX = 5;

    private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().validate(rows -> {
        if (rows.isEmpty() || rows.size() > MAX) return DataResult.error(() -> "Pattern must have 1-5 rows");
        int w = rows.get(0).length();
        for (String r : rows) {
            if (r.length() != w || w == 0 || w > MAX) return DataResult.error(() -> "Pattern rows must be equal width, 1-5 chars");
        }
        return DataResult.success(rows);
    });
    private static final Codec<String> KEY_CODEC = Codec.STRING.validate(s ->
            s.length() == 1 && !s.equals(" ") ? DataResult.success(s) : DataResult.error(() -> "Invalid key '" + s + "'"));

    public static final MapCodec<VoidCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PATTERN_CODEC.fieldOf("pattern").forGetter(r -> r.pattern),
            Codec.unboundedMap(KEY_CODEC, Ingredient.DISALLOW_EMPTY_CODEC).fieldOf("key").forGetter(r -> r.key),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, VoidCraftingRecipe::new));

    public static final PacketCodec<RegistryByteBuf, VoidCraftingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), r -> r.pattern,
            PacketCodecs.map(HashMap::new, PacketCodecs.STRING, Ingredient.PACKET_CODEC), r -> new HashMap<>(r.key),
            ItemStack.PACKET_CODEC, r -> r.result,
            VoidCraftingRecipe::new);

    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final ItemStack result;
    private final int width, height;
    private final DefaultedList<Ingredient> ingredients;

    public VoidCraftingRecipe(List<String> pattern, Map<String, Ingredient> key, ItemStack result) {
        this.pattern = List.copyOf(pattern);
        this.key = Map.copyOf(key);
        this.result = result;
        this.height = pattern.size();
        this.width = pattern.get(0).length();
        this.ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = pattern.get(y).charAt(x);
                if (c == ' ') continue;
                Ingredient ing = key.get(String.valueOf(c));
                if (ing == null) throw new IllegalArgumentException("Pattern references undefined key '" + c + "'");
                ingredients.set(x + y * width, ing);
            }
        }
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getWidth() != width || input.getHeight() != height) return false;
        return matches(input, false) || matches(input, true);
    }

    private boolean matches(CraftingRecipeInput input, boolean mirrored) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ingredient ing = ingredients.get((mirrored ? width - 1 - x : x) + y * width);
                if (!ing.test(input.getStackInSlot(x, y))) return false;
            }
        }
        return true;
    }

    @Override public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) { return result.copy(); }
    @Override public boolean fits(int w, int h) { return w >= width && h >= height; }
    @Override public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) { return result; }
    @Override public DefaultedList<Ingredient> getIngredients() { return ingredients; }
    @Override public boolean isIgnoredInRecipeBook() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.VOID_CRAFTING_SERIALIZER; }
    @Override public RecipeType<?> getType() { return ModRecipes.VOID_CRAFTING; }
}
