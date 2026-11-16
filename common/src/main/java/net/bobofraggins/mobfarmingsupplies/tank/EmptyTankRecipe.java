package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * A shapeless recipe that uses up a tank, which must be empty so no fluid is lost (the Color
 * Workstation). Type {@code mobfarmingsupplies:empty_tank} in JSON, written like a shapeless
 * recipe: it reads and syncs through vanilla's shapeless codecs.
 */
public class EmptyTankRecipe extends ShapelessRecipe {

    public static final MapCodec<EmptyTankRecipe> MAP_CODEC =
            RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(EmptyTankRecipe::from, r -> r);

    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyTankRecipe> STREAM_CODEC =
            RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(EmptyTankRecipe::from, r -> r);

    public EmptyTankRecipe(String group, CraftingBookCategory category, ItemStack result,
                           NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    private static EmptyTankRecipe from(ShapelessRecipe recipe) {
        // getResultItem ignores its registries argument for shapeless recipes.
        return new EmptyTankRecipe(recipe.getGroup(), recipe.category(), recipe.getResultItem(null),
                recipe.getIngredients());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) return false;
        }
        return super.matches(input, level);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.EMPTY_TANK_SERIALIZER.get();
    }
}
