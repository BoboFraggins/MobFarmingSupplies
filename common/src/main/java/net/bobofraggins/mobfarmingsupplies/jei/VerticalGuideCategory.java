package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A JEI category showing a {@link GuideRecipe}: its steps top-to-bottom with downward arrows
 * between each (a plain vertical guide, not a real crafting recipe).
 *
 * <p>Structure mirrors TremendousStorage's {@code PositiveVibesCauldronCategory}. Used by the
 * XP Juice guide and the Anvil Crushing guide (see {@link MobFarmingSuppliesJeiPlugin}).
 */
public class VerticalGuideCategory implements IRecipeCategory<GuideRecipe> {

    private static final int SLOT_X = 4;
    private static final int STEP_SPACING = 24;
    private static final int SLOT_SIZE = 18;
    private static final int WIDTH = 26;
    private static final int ARROW_COLOR = 0xFF555555;

    private final IRecipeType<GuideRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final int height;

    /** @param steps number of steps each recipe of this category shows (sets the height) */
    public VerticalGuideCategory(IRecipeType<GuideRecipe> recipeType, Component title, IDrawable icon, int steps) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.height = (steps - 1) * STEP_SPACING + SLOT_SIZE;
    }

    @Override
    public IRecipeType<GuideRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GuideRecipe recipe, IFocusGroup focuses) {
        var steps = recipe.steps();
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i);
            builder.addSlot(step.role(), SLOT_X, i * STEP_SPACING)
                    .addItemStacks(step.stacks())
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(step.tooltip()));
        }
    }

    @Override
    public void draw(
            GuideRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        for (int i = 1; i < recipe.steps().size(); i++) {
            drawDownArrow(guiGraphics, i * STEP_SPACING - 5);
        }
    }

    /**
     * Draws a small downward-pointing triangle (5×3 px) centred horizontally in the background,
     * with its top row at {@code y}.
     */
    private static void drawDownArrow(GuiGraphicsExtractor g, int y) {
        int cx = WIDTH / 2;
        g.fill(cx - 2, y, cx + 3, y + 1, ARROW_COLOR);
        g.fill(cx - 1, y + 1, cx + 2, y + 2, ARROW_COLOR);
        g.fill(cx, y + 2, cx + 1, y + 3, ARROW_COLOR);
    }
}
