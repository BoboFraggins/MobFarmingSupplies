package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * JEI page for each {@link Essence}: the mob (upside down, having been unalived), plus its Essence
 * Filter above an Essence Hopper, gives the essence. Asking for the essence's bucket finds it too.
 */
public final class EssenceExtractionCategory implements IRecipeCategory<Essence> {

    public static final RecipeType<Essence> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "essence_extraction", Essence.class);

    private static final int SLOT = 18;
    private static final int MOB_WIDTH = 44;
    private static final int PLUS_X = MOB_WIDTH + 4;
    private static final int STACK_X = MOB_WIDTH + 11;
    private static final int ARROW_X = STACK_X + SLOT + 5;
    private static final int OUTPUT_X = ARROW_X + 24 + 5;
    /** The output slot's background is wider than its slot, and shouldn't touch the page's edge. */
    private static final int OUTPUT_MARGIN = 5;
    private static final int WIDTH = OUTPUT_X + SLOT + OUTPUT_MARGIN;
    private static final int HEIGHT = 2 * SLOT + 4;
    private static final int ARROW_HEIGHT = 17;
    /** Vanilla draws a mob with this name upside down. */
    private static final Component UPSIDE_DOWN_NAME = Component.literal("Dinnerbone");

    private final Component title = Component.translatable("jei.mobfarmingsupplies.essence_extraction");
    private final IDrawable icon;
    private final IDrawable arrow;
    private final long bucketVolume;
    private final MobDisplay mobs = new MobDisplay();

    public EssenceExtractionCategory(IJeiHelpers helpers) {
        this.icon = helpers.getGuiHelper().createDrawableItemLike(Registration.ESSENCE_HOPPER_ITEM.get());
        this.arrow = helpers.getGuiHelper().getRecipeArrow();
        this.bucketVolume = helpers.getPlatformFluidHelper().bucketVolume();
    }

    public static List<Essence> recipes() {
        return List.of(Essence.values());
    }

    @Override
    public RecipeType<Essence> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, Essence essence, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, STACK_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(Registration.ESSENCE_FILTERS.get(essence).get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.essence_extraction.step.filter")));
        builder.addSlot(RecipeIngredientRole.CATALYST, STACK_X + 1, HEIGHT - SLOT + 1)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(Registration.ESSENCE_HOPPER_ITEM.get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.essence_extraction.step.hopper")));
        // In the config's millibuckets, whatever unit this loader's fluids are counted in.
        long amount = MFSConfig.getEssencePerDeath() * bucketVolume / 1000;
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, (HEIGHT - SLOT) / 2 + 1)
                .setOutputSlotBackground()
                .setFluidRenderer(Math.max(amount, 1), false, 16, 16) // a full slot, not a sliver of a bucket
                .addFluidStack(Registration.ESSENCE_SOURCES.get(essence).get(), amount)
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.essence_extraction.step.fluid")));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStack(new ItemStack(Registration.ESSENCE_BUCKETS.get(essence).get()));
    }

    @Override
    public void draw(Essence essence, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        LivingEntity mob = unalivedMob(essence);
        if (mob != null) MobDisplay.draw(graphics, mob, 0, 0, MOB_WIDTH, HEIGHT, mouseX, mouseY);
        VerticalGuideCategory.drawPlus(graphics, PLUS_X, HEIGHT / 2);
        arrow.draw(graphics, ARROW_X, (HEIGHT - ARROW_HEIGHT) / 2);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Essence essence, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= MOB_WIDTH) return;
        tooltip.add(essence.mob().getDescription());
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.essence_extraction.step.mob"));
    }

    @Nullable
    private LivingEntity unalivedMob(Essence essence) {
        LivingEntity mob = mobs.get(essence.mob(), false);
        if (mob != null) mob.setCustomName(UPSIDE_DOWN_NAME);
        return mob;
    }
}
