package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Data carrier for a vertical "how do I get this" guide shown in JEI by a
 * {@link VerticalGuideCategory}: a few items top-to-bottom with arrows between them.
 */
public record GuideRecipe(List<Step> steps) {

    /**
     * One row in the guide: the item(s) to display (JEI cycles through several), their JEI
     * role, and a tooltip hint.
     */
    public record Step(List<ItemStack> stacks, RecipeIngredientRole role, Component tooltip) {
        public Step(ItemStack stack, RecipeIngredientRole role, Component tooltip) {
            this(List.of(stack), role, tooltip);
        }
    }
}
