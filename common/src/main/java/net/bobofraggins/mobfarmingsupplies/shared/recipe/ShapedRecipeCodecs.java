package net.bobofraggins.mobfarmingsupplies.shared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Serializers for this mod's {@link ShapedRecipe} and {@link ShapelessRecipe} subclasses: the same
 * JSON and network form as the vanilla recipe (read by vanilla's serializer), built by the
 * subclass's own constructor.
 */
public final class ShapedRecipeCodecs {

    private ShapedRecipeCodecs() {}

    /** A shaped recipe's grid (1.20.5+ has a class for it; 1.20.1 keeps the parts on the recipe). */
    public record ShapedRecipePattern(int width, int height, NonNullList<Ingredient> ingredients) {}

    /** A shaped recipe subclass that keeps its pattern and result. */
    public interface Shaped {
        ShapedRecipePattern pattern();

        ItemStack result();
    }

    @FunctionalInterface
    public interface Factory<T> {
        T create(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                 ItemStack result, boolean showNotification);
    }

    @FunctionalInterface
    public interface ShapelessFactory<T> {
        T create(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
                 NonNullList<Ingredient> ingredients);
    }

    public static <T extends ShapedRecipe> RecipeSerializer<T> serializer(Factory<T> factory) {
        return new RecipeSerializer<>() {
            @Override
            public T fromJson(ResourceLocation id, JsonObject json) {
                return from(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
            }

            @Override
            public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                return from(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf));
            }

            @Override
            public void toNetwork(FriendlyByteBuf buf, T recipe) {
                RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
            }

            private T from(ShapedRecipe r) {
                return factory.create(r.getId(), r.getGroup(), r.category(),
                        new ShapedRecipePattern(r.getWidth(), r.getHeight(), r.getIngredients()),
                        r.getResultItem(null), r.showNotification());
            }
        };
    }

    /**
     * A shapeless recipe whose result may carry NBT ({@code "result": {"item", "count", "nbt"}}):
     * 1.20.1's own shapeless recipe drops it on Fabric (Forge reads it). Sent to clients as vanilla's.
     */
    public static RecipeSerializer<ShapelessRecipe> shapelessWithNbt() {
        return new RecipeSerializer<>() {
            @Override
            public ShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
                ShapelessRecipe r = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
                ItemStack result = net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs.RESULT_STACK
                        .parse(com.mojang.serialization.JsonOps.INSTANCE, json.get("result"))
                        .getOrThrow(false, msg -> {});
                return new ShapelessRecipe(id, r.getGroup(), r.category(), result, r.getIngredients());
            }

            @Override
            public ShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                return RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf);
            }

            @Override
            public void toNetwork(FriendlyByteBuf buf, ShapelessRecipe recipe) {
                RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
            }
        };
    }

    public static <T extends ShapelessRecipe> RecipeSerializer<T> shapelessSerializer(ShapelessFactory<T> factory) {
        return new RecipeSerializer<>() {
            @Override
            public T fromJson(ResourceLocation id, JsonObject json) {
                return from(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json));
            }

            @Override
            public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                return from(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf));
            }

            @Override
            public void toNetwork(FriendlyByteBuf buf, T recipe) {
                RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
            }

            private T from(ShapelessRecipe r) {
                return factory.create(r.getId(), r.getGroup(), r.category(), r.getResultItem(null), r.getIngredients());
            }
        };
    }
}
