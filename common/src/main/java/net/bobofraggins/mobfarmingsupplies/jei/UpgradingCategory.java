package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradingRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** JEI page for the Upgrade Station's {@link UpgradingRecipe}s, one row each: the item, the input, an arrow, the result. */
public final class UpgradingCategory implements IRecipeCategory<UpgradingCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "upgrading", Display.class);

    /** One row as shown. */
    public record Display(List<ItemStack> items, List<ItemStack> inputs, ItemStack output) {}

    private static final int SLOT = 18;
    private static final int ITEM_X = 0, INPUT_X = 24, ARROW_X = 46, OUTPUT_X = 76;
    private static final int WIDTH = OUTPUT_X + SLOT;
    private static final int HEIGHT = SLOT;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.upgrading");
    private final IDrawable icon;
    private final IDrawable arrow;

    public UpgradingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(Registration.UPGRADE_STATION_ITEM.get());
        this.arrow = helper.getRecipeArrow();
    }

    /** Every row to show for {@code recipes}. */
    public static List<Display> displays(List<UpgradingRecipe> recipes) {
        return recipes.stream()
                .map(recipe -> new Display(stacks(recipe.item()), stacks(recipe.input()), recipe.result().create()))
                .toList();
    }

    private static List<ItemStack> stacks(Ingredient ingredient) {
        return ingredient.items().map(item -> new ItemStack(item)).toList();
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return RECIPE_TYPE;
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
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, ITEM_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.items());
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.inputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 1)
                .setOutputSlotBackground()
                .add(display.output());
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        arrow.draw(g, ARROW_X, 1);
    }
}
