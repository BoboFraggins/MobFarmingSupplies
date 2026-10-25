package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A JEI category showing a {@link GuideRecipe} the way the Filter Scribing Terminal page does: in
 * one of the mod's dialogs, the full width of a recipe page, titled with the recipe's machine. Its
 * rows run left to right with JEI's recipe arrow between them, each row one slot or a few with
 * plus signs between them. Arrows and plus signs sit on the items' centre line.
 *
 * <p>A subclass can end the line with something other than slots (the cloning page's mob): it
 * passes its size to the constructor and draws it in {@link #drawTrailing}, after an arrow.
 */
public class DialogGuideCategory implements IRecipeCategory<GuideRecipe> {

    /** As wide as a machine's window (and the Filter Scribing Terminal page). */
    private static final int WIDTH = 176;
    /** A slot's cell: its 16 px item with the 1 px background border round it. */
    private static final int CELL = 18;
    private static final int ITEM = 16;
    /** Space either side of an arrow or plus sign. */
    private static final int GAP = 4;
    /** Space above and below the tallest thing on the line. */
    private static final int PADDING = 4;

    private final IRecipeType<GuideRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final IDrawableStatic plus;
    private final int trailingWidth;
    private final Dialog dialog;
    /** The line everything is centred on: the items' middle, between two pixel rows. */
    private final int centreY;

    public DialogGuideCategory(IRecipeType<GuideRecipe> recipeType, Component title, IDrawable icon, IGuiHelper helper) {
        this(recipeType, title, icon, helper, 0, 0);
    }

    /** @param trailingWidth,trailingHeight the size of what {@link #drawTrailing} draws, or 0 for none */
    protected DialogGuideCategory(IRecipeType<GuideRecipe> recipeType, Component title, IDrawable icon,
                                  IGuiHelper helper, int trailingWidth, int trailingHeight) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.arrow = helper.getRecipeArrow();
        this.plus = helper.getRecipePlusSign();
        this.trailingWidth = trailingWidth;
        int bodyH = Math.max(CELL, trailingHeight) + 2 * PADDING;
        bodyH += bodyH % 2; // even, so the centre line falls between pixel rows like an item's middle
        this.dialog = new Dialog(Dialog.blankPane(WIDTH, bodyH));
        dialog.init(0, 0);
        this.centreY = Dialog.TITLE_H + bodyH / 2;
    }

    private int rowWidth(int slots) {
        return slots * CELL + (slots - 1) * (plus.getWidth() + 2 * GAP);
    }

    private int arrowSpan() {
        return arrow.getWidth() + 2 * GAP;
    }

    /** Left of each row's first cell, then of the trailing part (if any), the whole line centred. */
    private int[] rowXs(List<GuideRecipe.Row> rows) {
        int[] xs = new int[rows.size() + 1];
        int total = 0;
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) total += arrowSpan();
            xs[r] = total;
            total += rowWidth(rows.get(r).steps().size());
        }
        if (trailingWidth > 0) {
            total += arrowSpan();
            xs[rows.size()] = total;
            total += trailingWidth;
        }
        int left = (WIDTH - total) / 2;
        for (int r = 0; r < xs.length; r++) xs[r] += left;
        return xs;
    }

    private int cellX(int rowX, int i) {
        return rowX + i * (CELL + plus.getWidth() + 2 * GAP);
    }

    /** Top of a drawable {@code height} tall centred on the line (an odd one sits half a pixel high). */
    private int centredTop(int height) {
        return centreY - (height + 1) / 2;
    }

    /** Left of the trailing part for {@code recipe}. */
    protected int trailingX(GuideRecipe recipe) {
        return rowXs(recipe.rows())[recipe.rows().size()];
    }

    /** Top of the trailing part. */
    protected int trailingY(int height) {
        return centredTop(height);
    }

    /** Draws the trailing part at ({@code x}, {@code y}); nothing by default. */
    protected void drawTrailing(GuideRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, int x, int y,
                                double mouseX, double mouseY) {}

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
        return dialog.totalWidth();
    }

    @Override
    public int getHeight() {
        return dialog.totalHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GuideRecipe recipe, IFocusGroup focuses) {
        var rows = recipe.rows();
        int[] xs = rowXs(rows);
        int itemY = centreY - ITEM / 2;
        for (int r = 0; r < rows.size(); r++) {
            var steps = rows.get(r).steps();
            for (int i = 0; i < steps.size(); i++) {
                var step = steps.get(i);
                builder.addSlot(step.role(), cellX(xs[r], i) + 1, itemY)
                        .setStandardSlotBackground()
                        .addItemStacks(step.stacks())
                        .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(step.tooltip()));
            }
        }
    }

    @Override
    public void draw(GuideRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        // Mouse far away: purely visual, no hover highlights.
        dialog.render(g, Minecraft.getInstance().font, recipe.title() != null ? recipe.title() : title,
                -1000, -1000, 0);
        var rows = recipe.rows();
        int[] xs = rowXs(rows);
        int arrowTop = centredTop(arrow.getHeight());
        int plusTop = centredTop(plus.getHeight());
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) arrow.draw(g, xs[r] - arrowSpan() + GAP, arrowTop);
            int n = rows.get(r).steps().size();
            for (int i = 1; i < n; i++) {
                plus.draw(g, cellX(xs[r], i) - GAP - plus.getWidth(), plusTop);
            }
        }
        if (trailingWidth > 0) {
            int x = xs[rows.size()];
            arrow.draw(g, x - arrowSpan() + GAP, arrowTop);
            drawTrailing(recipe, slots, g, x, 0, mouseX, mouseY);
        }
    }
}
