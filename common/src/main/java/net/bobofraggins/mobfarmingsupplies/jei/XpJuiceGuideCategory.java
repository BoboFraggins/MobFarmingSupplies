package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * JEI recipe category showing how an XP Juice Bucket is obtained: Experience Syringe → Tank →
 * XP Juice Bucket, top-to-bottom with downward arrows between each step.
 *
 * <p>Structure mirrors TremendousStorage's {@code PositiveVibesCauldronCategory} — a plain
 * vertical guide (not a real crafting recipe), reachable from JEI regardless of which of the
 * three items the player looks up (see the recipe-catalyst registrations in
 * {@link MobFarmingSuppliesJeiPlugin}).
 */
public class XpJuiceGuideCategory implements IRecipeCategory<XpJuiceGuideRecipe> {

    public static final RecipeType<XpJuiceGuideRecipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "xp_juice_guide", XpJuiceGuideRecipe.class);

    private static final int SLOT_X = 4;
    private static final int[] SLOT_YS = {0, 24, 48};
    private static final int WIDTH = 26;
    private static final int HEIGHT = 66;
    private static final int ARROW_COLOR = 0xFF555555;

    private final IDrawable icon;

    public XpJuiceGuideCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemLike(Registration.XP_JUICE_BUCKET.get());
    }

    @Override
    public RecipeType<XpJuiceGuideRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mobfarmingsupplies.xp_juice_guide");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, XpJuiceGuideRecipe recipe, IFocusGroup focuses) {
        var steps = recipe.steps();
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i);
            builder.addSlot(step.role(), SLOT_X, SLOT_YS[i])
                    .addItemStack(step.stack())
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(step.tooltip()));
        }
    }

    @Override
    public void draw(
            XpJuiceGuideRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        int count = recipe.steps().size();
        for (int i = 1; i < count; i++) {
            drawDownArrow(guiGraphics, SLOT_YS[i] - 5);
        }
    }

    /**
     * Draws a small downward-pointing triangle (5×3 px) centred horizontally in the background,
     * with its top row at {@code y}.
     */
    private void drawDownArrow(GuiGraphicsExtractor g, int y) {
        int cx = WIDTH / 2;
        g.fill(cx - 2, y, cx + 3, y + 1, ARROW_COLOR);
        g.fill(cx - 1, y + 1, cx + 2, y + 2, ARROW_COLOR);
        g.fill(cx, y + 2, cx + 1, y + 3, ARROW_COLOR);
    }
}
