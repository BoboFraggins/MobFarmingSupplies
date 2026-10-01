package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalMenu;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.ScribingControlsPane;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI recipe for scribing Item Filters at the Filter Scribing Terminal. Draws the terminal's own
 * UI (title bar + controls pane, not clickable) with JEI slots where the terminal's are:
 * a Blank Filter on the left, the example matcher item in the ghost slot, and the resulting Item
 * Filter on the right. The matcher and result slots cycle through {@link FilterScribingExamples}
 * together; the drawn Is / Is Not toggle and selected list row follow whichever result JEI is
 * currently showing, so hovering the result shows the matching filter's name.
 */
public class FilterScribingCategory implements IRecipeCategory<FilterScribingExamples.Recipe> {

    public static final IRecipeType<FilterScribingExamples.Recipe> RECIPE_TYPE =
            IRecipeType.create(MobFarmingSuppliesCommon.MODID, "filter_scribing", FilterScribingExamples.Recipe.class);

    private static final Component TITLE = Component.translatable("block.mobfarmingsupplies.filter_scribing_terminal");

    private final IDrawable icon;
    private final ScribingControlsPane pane =
            new ScribingControlsPane(() -> ItemStack.EMPTY, item -> List.of(), p -> {});
    private final Dialog dialog = new Dialog(pane);
    private final int ghostX;
    private final int ghostY;

    public FilterScribingCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(Registration.FILTER_SCRIBING_TERMINAL_ITEM.get());
        dialog.init(0, 0);
        ghostX = ScribingControlsPane.ghostItemX();
        ghostY = dialog.getPaneAbsY(0) + ScribingControlsPane.ghostItemY();
    }

    @Override
    public IRecipeType<FilterScribingExamples.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, FilterScribingExamples.Recipe recipe, IFocusGroup focuses) {
        var examples = recipe.examples();
        builder.addSlot(RecipeIngredientRole.INPUT, FilterScribingTerminalMenu.INPUT_X, FilterScribingTerminalMenu.SLOT_Y)
                .add(new ItemStack(Registration.BLANK_FILTER.get()));
        // Render-only: the example items aren't ingredients, so they shouldn't gain a JEI "use".
        IRecipeSlotBuilder matcher = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, ghostX, ghostY)
                .addItemStacks(examples.stream().map(FilterScribingExamples.Example::matcherItem).toList());
        IRecipeSlotBuilder result = builder.addSlot(RecipeIngredientRole.OUTPUT,
                        FilterScribingTerminalMenu.OUTPUT_X, FilterScribingTerminalMenu.SLOT_Y)
                .addItemStacks(examples.stream().map(FilterScribingExamples.Example::filter).toList());
        builder.createFocusLink(matcher, result); // cycle the two in step
    }

    @Override
    public void draw(
            FilterScribingExamples.Recipe recipe,
            IRecipeSlotsView slots,
            GuiGraphicsExtractor g,
            double mouseX,
            double mouseY) {
        FilterScribingExamples.Example shown = currentExample(recipe, slots);
        if (shown != null) {
            pane.showExample(shown.rows(), shown.negate(), shown.selectedRow());
        } else {
            pane.showExample(List.of(), false, -1);
        }
        // Mouse far away: the preview is purely visual, no hover highlights.
        dialog.render(g, Minecraft.getInstance().font, TITLE, -1000, -1000, 0);
        FilterScribingTerminalScreen.drawSlot(g, FilterScribingTerminalMenu.INPUT_X, FilterScribingTerminalMenu.SLOT_Y);
        FilterScribingTerminalScreen.drawSlot(g, FilterScribingTerminalMenu.OUTPUT_X, FilterScribingTerminalMenu.SLOT_Y);
    }

    /** The example whose Item Filter JEI is currently displaying in the result slot. */
    private static FilterScribingExamples.Example currentExample(FilterScribingExamples.Recipe recipe, IRecipeSlotsView slots) {
        var outputs = slots.getSlotViews(RecipeIngredientRole.OUTPUT);
        if (outputs.isEmpty()) return null;
        ItemStack displayed = outputs.getFirst().getDisplayedItemStack().orElse(ItemStack.EMPTY);
        for (var example : recipe.examples()) {
            if (ItemStack.isSameItemSameComponents(example.filter(), displayed)) return example;
        }
        return null;
    }
}
