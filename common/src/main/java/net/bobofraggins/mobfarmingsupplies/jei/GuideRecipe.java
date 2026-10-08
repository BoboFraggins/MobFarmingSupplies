package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Data carrier for a vertical "how do I get this" guide shown in JEI by a
 * {@link VerticalGuideCategory}: rows top-to-bottom with arrows between them, each row one item or
 * a few side by side with plus signs between them.
 */
public record GuideRecipe(List<Row> rows) {

    /** A guide with one item per row. */
    public static GuideRecipe column(Step... steps) {
        return new GuideRecipe(java.util.Arrays.stream(steps).map(step -> new Row(List.of(step))).toList());
    }

    /** A row of items shown side by side ("this + that"). */
    public record Row(List<Step> steps) {}

    /**
     * One item in the guide: the item(s) to display (JEI cycles through several), their JEI
     * role, and a tooltip hint.
     */
    public record Step(List<ItemStack> stacks, RecipeIngredientRole role, Component tooltip) {
        public Step(ItemStack stack, RecipeIngredientRole role, Component tooltip) {
            this(List.of(stack), role, tooltip);
        }
    }
}
