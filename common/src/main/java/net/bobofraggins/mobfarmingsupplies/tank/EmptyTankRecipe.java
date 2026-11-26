package net.bobofraggins.mobfarmingsupplies.tank;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * A shapeless recipe that uses up a tank, which must be empty so no fluid is lost (the Color
 * Workstation). Type {@code mobfarmingsupplies:empty_tank} in JSON, written like a shapeless
 * recipe: it reads and syncs through vanilla's shapeless serializer.
 */
public class EmptyTankRecipe extends ShapelessRecipe {

    public static final RecipeSerializer<EmptyTankRecipe> SERIALIZER =
            ShapedRecipeCodecs.shapelessSerializer(EmptyTankRecipe::new);

    public EmptyTankRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
                           NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        for (ItemStack ingredient : input.getItems()) {
            TankContents contents = ItemComponents.get(ingredient, Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) return false;
        }
        return super.matches(input, level);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.EMPTY_TANK_SERIALIZER.get();
    }
}
