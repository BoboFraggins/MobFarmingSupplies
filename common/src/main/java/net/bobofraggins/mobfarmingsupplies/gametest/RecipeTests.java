package net.bobofraggins.mobfarmingsupplies.gametest;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;

/**
 * Every crafting, cooking and stonecutting recipe of the mod, filled in with the first item of each
 * of its own ingredients, matches and makes something. Catches ingredients that resolve to nothing
 * (an empty tag on this loader), patterns that can't be filled, and results that come out empty.
 * The mod's own machine recipes are covered by those machines' tests.
 */
final class RecipeTests {

    private RecipeTests() {}

    static List<TestFunction> all() {
        return List.of(GameTestKit.test("recipes", "craftable", 20, RecipeTests::run));
    }

    private static void run(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RegistryAccess access = level.registryAccess();
        GameTestKit.Problems problems = new GameTestKit.Problems();
        int checked = 0;
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            if (!GameTestKit.isOurs(recipe.getId())) continue;
            String problem;
            if (recipe instanceof CraftingRecipe crafting) {
                if (crafting.isSpecial()) continue;
                problem = crafting(crafting, level, access);
            } else if (recipe instanceof AbstractCookingRecipe || recipe instanceof SingleItemRecipe) {
                problem = single(recipe, level, access);
            } else {
                continue;
            }
            checked++;
            if (problem != null) problems.add(recipe.getId() + ": " + problem);
        }
        if (checked == 0) problems.add("found none of the mod's recipes");
        problems.finish(helper);
    }

    private static String crafting(CraftingRecipe recipe, ServerLevel level, RegistryAccess access) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        CraftingContainer grid = new TransientCraftingContainer(new NoMenu(), 3, 3);
        if (recipe instanceof ShapedRecipe shaped) {
            for (int y = 0; y < shaped.getHeight(); y++) {
                for (int x = 0; x < shaped.getWidth(); x++) {
                    String bad = put(grid, y * 3 + x, ingredients.get(y * shaped.getWidth() + x));
                    if (bad != null) return bad;
                }
            }
        } else {
            if (ingredients.size() > 9) return "has " + ingredients.size() + " ingredients";
            for (int i = 0; i < ingredients.size(); i++) {
                String bad = put(grid, i, ingredients.get(i));
                if (bad != null) return bad;
            }
        }
        if (!recipe.matches(grid, level)) return "doesn't match a grid of its own ingredients " + grid.getItems();
        ItemStack made = recipe.assemble(grid, access);
        if (made.isEmpty()) return "makes nothing from its own ingredients";
        return null;
    }

    /**
     * Puts the ingredient's first item in the slot. An ingredient listing no items is a blank
     * square (vanilla's empty ingredient), or an empty tag, which then shows up as a mismatch.
     * Doesn't ask {@code isEmpty()}: Forge's custom ingredients say they're empty when they aren't.
     */
    private static String put(CraftingContainer grid, int slot, Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0) grid.setItem(slot, items[0].copyWithCount(1));
        return null;
    }

    private static String single(Recipe<?> recipe, ServerLevel level, RegistryAccess access) {
        Ingredient ingredient = recipe.getIngredients().get(0);
        if (ingredient.getItems().length == 0) return "its ingredient matches no items on this loader";
        SimpleContainer input = new SimpleContainer(3);
        input.setItem(0, ingredient.getItems()[0].copyWithCount(1));
        @SuppressWarnings("unchecked")
        Recipe<SimpleContainer> typed = (Recipe<SimpleContainer>) recipe;
        if (!typed.matches(input, level)) return "doesn't match its own ingredient";
        if (typed.assemble(input, access).isEmpty()) return "makes nothing";
        return null;
    }

    /** A crafting grid needs a menu to tell about changes; nothing listens here. */
    private static final class NoMenu extends AbstractContainerMenu {
        NoMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
