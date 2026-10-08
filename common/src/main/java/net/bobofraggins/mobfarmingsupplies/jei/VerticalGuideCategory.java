package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A JEI category showing a {@link GuideRecipe}: its rows top-to-bottom with downward arrows
 * between them, each row one item or a few side by side with plus signs between them (a plain
 * vertical guide, not a real crafting recipe).
 *
 * <p>Structure mirrors TremendousStorage's {@code PositiveVibesCauldronCategory}. Used by the
 * XP Juice guide and the Anvil Crushing guide (see {@link MobFarmingSuppliesJeiPlugin}).
 */
public class VerticalGuideCategory implements IRecipeCategory<GuideRecipe> {

    private static final int STEP_SPACING = 24;
    private static final int SLOT_SIZE = 18;
    /** Room for a plus sign between two slots in a row. */
    private static final int PLUS_GAP = 11;
    private static final int MARGIN = 4;
    private static final int ARROW_COLOR = 0xFF555555;

    private final RecipeType<GuideRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final int height;
    private final int width;

    /** @param steps number of rows each recipe of this category shows (sets the height) */
    public VerticalGuideCategory(RecipeType<GuideRecipe> recipeType, Component title, IDrawable icon, int steps) {
        this(recipeType, title, icon, steps, 1);
    }

    /**
     * @param rows    number of rows each recipe of this category shows (sets the height)
     * @param columns most items side by side in a row (sets the width)
     */
    public VerticalGuideCategory(RecipeType<GuideRecipe> recipeType, Component title, IDrawable icon,
                                 int rows, int columns) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.height = (rows - 1) * STEP_SPACING + SLOT_SIZE;
        this.width = rowWidth(columns) + 2 * MARGIN;
    }

    private static int rowWidth(int slots) {
        return slots * SLOT_SIZE + (slots - 1) * PLUS_GAP;
    }

    /** X of the {@code i}th of {@code n} slots in a row, the row centred. */
    private int slotX(int i, int n) {
        return (width - rowWidth(n)) / 2 + i * (SLOT_SIZE + PLUS_GAP);
    }

    @Override
    public RecipeType<GuideRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return width;
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
        var rows = recipe.rows();
        for (int r = 0; r < rows.size(); r++) {
            var steps = rows.get(r).steps();
            for (int i = 0; i < steps.size(); i++) {
                var step = steps.get(i);
                builder.addSlot(step.role(), slotX(i, steps.size()), r * STEP_SPACING)
                        .addItemStacks(step.stacks())
                        .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(step.tooltip()));
            }
        }
    }

    @Override
    public void draw(
            GuideRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        var rows = recipe.rows();
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) drawDownArrow(guiGraphics, r * STEP_SPACING - 5);
            int n = rows.get(r).steps().size();
            for (int i = 1; i < n; i++) {
                drawPlus(guiGraphics, slotX(i, n) - PLUS_GAP / 2 - 1, r * STEP_SPACING + SLOT_SIZE / 2);
            }
        }
    }

    /**
     * Draws a small downward-pointing triangle (5×3 px) centred horizontally in the background,
     * with its top row at {@code y}.
     */
    private void drawDownArrow(GuiGraphicsExtractor g, int y) {
        int cx = width / 2;
        g.fill(cx - 2, y, cx + 3, y + 1, ARROW_COLOR);
        g.fill(cx - 1, y + 1, cx + 2, y + 2, ARROW_COLOR);
        g.fill(cx, y + 2, cx + 1, y + 3, ARROW_COLOR);
    }

    /** Draws a small plus sign (5×5 px) centred on ({@code cx}, {@code cy}). */
    private static void drawPlus(GuiGraphicsExtractor g, int cx, int cy) {
        g.fill(cx - 2, cy, cx + 3, cy + 1, ARROW_COLOR);
        g.fill(cx, cy - 2, cx + 1, cy + 3, ARROW_COLOR);
    }
}
