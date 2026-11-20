package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlockEntity;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page for the Color Workstation: {@value ColorWorkstationBlockEntity#MB_PER_DYE} mB of
 * Essence of Color makes one of each vanilla dye. One recipe per dye, so looking a dye up finds
 * just its own; looking up the essence (or its bucket or bottle) finds all sixteen.
 */
public final class DyeMixingCategory implements IRecipeCategory<DyeMixingCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "dye_mixing", Display.class);

    /** One dye the workstation makes. */
    public record Display(Item dye) {}

    private static final int SLOT = 18;
    private static final int INPUT_X = 12, ARROW_X = 40, OUTPUT_X = 76;
    private static final int WIDTH = OUTPUT_X + SLOT + 12;
    private static final int TEXT_Y = SLOT + 4;
    private static final int HEIGHT = TEXT_Y + 9;
    private static final int TEXT_COLOR = 0xFF808080;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.dye_mixing");
    private final IDrawable icon;
    private final IDrawable arrow;
    private final long bucketVolume;

    public DyeMixingCategory(IJeiHelpers helpers) {
        this.icon = helpers.getGuiHelper().createDrawableItemLike(Registration.COLOR_WORKSTATION_ITEM.get());
        this.arrow = helpers.getGuiHelper().getRecipeArrow();
        this.bucketVolume = helpers.getPlatformFluidHelper().bucketVolume();
    }

    public static List<Display> displays() {
        return ColorWorkstationBlockEntity.DYES.stream().map(Display::new).toList();
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
        // In millibuckets, whatever unit this loader's fluids are counted in.
        long amount = ColorWorkstationBlockEntity.MB_PER_DYE * bucketVolume / 1000;
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, 1)
                .setStandardSlotBackground()
                .setFluidRenderer(Math.max(amount, 1), false, 16, 16) // a full slot, not a sliver
                .addFluidStack(Registration.ESSENCE_SOURCES.get(Essence.COLOR).get(), amount)
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.dye_mixing.step.essence")));
        // Buckets and bottles of it fill the workstation too.
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStack(new ItemStack(Registration.ESSENCE_BUCKETS.get(Essence.COLOR).get()))
                .addItemStack(new ItemStack(Registration.ESSENCE_BOTTLES.get(Essence.COLOR).get()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 1)
                .setOutputSlotBackground()
                .addItemStack(new ItemStack(display.dye()));
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        arrow.draw(g, ARROW_X, 1);
        Font font = Minecraft.getInstance().font;
        Component text = Component.translatable("jei.mobfarmingsupplies.dye_mixing.cost",
                ColorWorkstationBlockEntity.MB_PER_DYE);
        g.text(font, text, (WIDTH - font.width(text)) / 2, TEXT_Y, TEXT_COLOR, false);
    }
}
