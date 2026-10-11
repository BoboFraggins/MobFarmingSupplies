package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.circuitinscriber.CircuitInscribingRecipe;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * JEI page for the Circuit Inscriber's recipes (synced from the server, see
 * {@code ClientInscribingRecipes}): its two inputs, a filling arrow and the output, laid out as in
 * the machine, with the craft's total energy and time underneath.
 */
public class CircuitInscribingCategory implements IRecipeCategory<CircuitInscribingRecipe> {

    public static final RecipeType<CircuitInscribingRecipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "circuit_inscribing", CircuitInscribingRecipe.class);

    private static final Component TITLE = Component.translatable("jei.mobfarmingsupplies.circuit_inscribing");
    private static final Identifier ARROW = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    /** How long the arrow takes to fill, looping. */
    private static final long ARROW_MILLIS = 2000;

    // Pane-local layout.
    private static final int WIDTH = 150;
    private static final int HEIGHT = 64;
    private static final int INPUT_X = 34;
    private static final int FIRST_Y = 4;
    private static final int SECOND_Y = 30;
    private static final int ARROW_X = 60;
    private static final int ARROW_Y = 18;
    private static final int OUTPUT_X = 94;
    private static final int OUTPUT_Y = 17;
    private static final int TEXT_Y = 52;

    private final IDrawable icon;
    /** The recipe being drawn, for the pane's energy and time line. */
    private CircuitInscribingRecipe drawing;
    private final Dialog dialog = new Dialog(new IDialogPane() {
        @Override
        public int preferredWidth() {
            return WIDTH;
        }

        @Override
        public int preferredHeight() {
            return HEIGHT;
        }

        @Override
        public void render(GuiGraphicsExtractor g, Font font, int width, int mx, int my, float partialTick) {
            PlayerInventoryPane.drawSlotBg(g, INPUT_X, FIRST_Y);
            PlayerInventoryPane.drawSlotBg(g, INPUT_X, SECOND_Y);
            PlayerInventoryPane.drawSlotBg(g, OUTPUT_X, OUTPUT_Y);
            g.fill(ARROW_X, ARROW_Y + 6, ARROW_X + 22, ARROW_Y + 10, 0xFF8B8B8B);
            int filled = (int) (24 * (Util.getMillis() % ARROW_MILLIS) / ARROW_MILLIS);
            if (filled > 0) g.blitSprite(RenderPipelines.GUI_TEXTURED, ARROW, 24, 16, 0, 0, ARROW_X, ARROW_Y, filled, 16);
            if (drawing != null) {
                Component cost = Component.translatable("jei.mobfarmingsupplies.circuit_inscribing.cost",
                        String.format("%,d", drawing.energy()), seconds(drawing.time()));
                g.text(font, cost, (width - font.width(cost)) / 2, TEXT_Y, 0xFF404040, false);
            }
        }
    });

    public CircuitInscribingCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(Registration.CIRCUIT_INSCRIBER_ITEM.get());
        dialog.init(0, 0);
    }

    /** Ticks as seconds, without a trailing ".0". */
    private static String seconds(int ticks) {
        return ticks % 20 == 0 ? String.valueOf(ticks / 20) : String.format("%.1f", ticks / 20f);
    }

    @Override
    public RecipeType<CircuitInscribingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, CircuitInscribingRecipe recipe, IFocusGroup focuses) {
        int x = dialog.getPaneAbsX(0);
        int y = dialog.getPaneAbsY(0);
        builder.addSlot(RecipeIngredientRole.INPUT, x + INPUT_X, y + FIRST_Y).add(recipe.first());
        recipe.second().ifPresent(second ->
                builder.addSlot(RecipeIngredientRole.INPUT, x + INPUT_X, y + SECOND_Y).add(second));
        builder.addSlot(RecipeIngredientRole.OUTPUT, x + OUTPUT_X, y + OUTPUT_Y).add(recipe.result().create());
    }

    @Override
    public void draw(CircuitInscribingRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g,
                     double mouseX, double mouseY) {
        drawing = recipe;
        dialog.render(g, Minecraft.getInstance().font, TITLE, -1000, -1000, 0);
    }
}
