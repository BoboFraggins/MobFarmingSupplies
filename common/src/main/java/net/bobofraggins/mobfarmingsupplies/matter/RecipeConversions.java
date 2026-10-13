package net.bobofraggins.mobfarmingsupplies.matter;

import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.circuitinscriber.CircuitInscribingRecipe;
import net.bobofraggins.mobfarmingsupplies.shearing.ShearingRecipe;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

/**
 * The {@link MatterConversion}s the loaded recipes describe. Most recipes (crafting, shaped or
 * shapeless, smelting and its relatives, stonecutting, smithing) list their ingredients through the
 * recipe book's placement information and their result through their display, so one path covers
 * them, including other mods' recipes built on them. This mod's Circuit Inscriber and Shearing
 * recipes are read directly; shearing doesn't use up its tool, so only the sheared item counts.
 * Recipes that can't be read this way, and special ones like dyeing armour, are left out.
 */
public final class RecipeConversions {

    private RecipeConversions() {}

    public static List<MatterConversion> collect(ServerLevel level) {
        List<MatterConversion> conversions = new ArrayList<>();
        ContextMap context = SlotDisplayContext.fromLevel(level);
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe.isSpecial()) continue;
            if (recipe instanceof CircuitInscribingRecipe inscribing) {
                List<Ingredient> inputs = new ArrayList<>();
                inputs.add(inscribing.first());
                inscribing.second().ifPresent(inputs::add);
                add(conversions, inputs, inscribing.result().create());
            } else if (recipe instanceof ShearingRecipe shearing) {
                add(conversions, List.of(shearing.ingredient()), shearing.result().create());
            } else {
                PlacementInfo placement = recipe.placementInfo();
                if (placement.isImpossibleToPlace()) continue;
                add(conversions, placement.ingredients(), result(recipe, context));
            }
        }
        return conversions;
    }

    private static void add(List<MatterConversion> conversions, List<Ingredient> ingredients, ItemStack result) {
        if (ingredients.isEmpty() || result.isEmpty()) return;
        List<MatterConversion.Input> inputs = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            List<Item> items = ingredient.items().map(Holder::value).distinct().toList();
            // An ingredient nothing can fill means the recipe can never be made.
            if (items.isEmpty()) return;
            inputs.add(new MatterConversion.Input(items, 1));
        }
        conversions.add(new MatterConversion(result.getItem(), result.getCount(), inputs));
    }

    private static ItemStack result(Recipe<?> recipe, ContextMap context) {
        for (RecipeDisplay display : recipe.display()) {
            ItemStack stack = display.result().resolveForFirstStack(context);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }
}
