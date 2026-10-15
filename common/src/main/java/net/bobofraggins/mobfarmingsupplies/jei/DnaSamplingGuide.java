package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleContents;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * JEI guide for DNA Samples: a DNA Collector, used on a mob, gives a DNA Sample of it. Shown as a
 * column, the collector, the mob, then the sample. The sample cycles through every mob the
 * collector can sample, and the mob drawn above it is always the one whose sample is showing.
 */
public final class DnaSamplingGuide implements IRecipeCategory<DnaSamplingGuide.Recipe> {

    public static final RecipeType<Recipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "dna_sampling", Recipe.class);

    /** One sample per mob the collector can sample, in name order. */
    public record Recipe(List<ItemStack> samples) {}

    private static final int WIDTH = 64;
    private static final int SLOT_SIZE = 18;
    private static final int MOB_TOP = 24;
    private static final int MOB_HEIGHT = 40;
    private static final int SAMPLE_TOP = MOB_TOP + MOB_HEIGHT + 8;
    private static final int HEIGHT = SAMPLE_TOP + SLOT_SIZE;
    private static final int ARROW_COLOR = 0xFF555555;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.dna_sampling");
    private final IDrawable icon;

    private final MobDisplay mobs = new MobDisplay();

    public DnaSamplingGuide(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(Registration.DNA_COLLECTOR.get());
    }

    /** The guide's one recipe: a sample for every living mob except players and those in {@code no_dna_sampling}. */
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct EntityType.is(TagKey) in 26.1.2
    public static Recipe recipe() {
        List<ItemStack> samples = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(DefaultAttributes::hasSupplier) // living mobs
                .filter(type -> type != EntityType.PLAYER)
                .filter(type -> !type.builtInRegistryHolder().is(MFSTags.EntityTypes.NO_DNA_SAMPLING))
                .sorted(Comparator.comparing(type -> type.getDescription().getString()))
                .map(DnaSamplingGuide::sampleOf)
                .toList();
        return new Recipe(samples);
    }

    /** A DNA Sample of {@code type}, as the collector would make (minus the mob's details). */
    static ItemStack sampleOf(EntityType<?> type) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        ItemStack sample = new ItemStack(Registration.DNA_SAMPLE.get());
        sample.set(Registration.DNA_SAMPLE_CONTENTS.get(),
                new DnaSampleContents(nbt, type.getDescription().getString()));
        return sample;
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        int x = (WIDTH - SLOT_SIZE) / 2;
        builder.addSlot(RecipeIngredientRole.INPUT, x, 0)
                .addItemStack(new ItemStack(Registration.DNA_COLLECTOR.get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.dna_sampling.step.collector")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, x, SAMPLE_TOP)
                .addItemStacks(recipe.samples())
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.dna_sampling.step.sample")));
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        drawDownArrow(graphics, SLOT_SIZE + 1);
        drawDownArrow(graphics, MOB_TOP + MOB_HEIGHT + 2);
        LivingEntity mob = shownMob(slots);
        if (mob == null) return;
        MobDisplay.draw(graphics, mob, 0, MOB_TOP, WIDTH, MOB_TOP + MOB_HEIGHT, mouseX, mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Recipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseY < MOB_TOP || mouseY >= MOB_TOP + MOB_HEIGHT) return;
        LivingEntity mob = shownMob(slots);
        if (mob != null) tooltip.add(mob.getType().getDescription());
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.dna_sampling.step.mob"));
    }

    /** The mob whose sample the output slot is showing right now, or null if it can't be drawn. */
    @Nullable
    private LivingEntity shownMob(IRecipeSlotsView slots) {
        ItemStack shown = slots.getSlotViews(RecipeIngredientRole.OUTPUT).stream().findFirst()
                .flatMap(view -> view.getDisplayedItemStack()).orElse(ItemStack.EMPTY);
        DnaSampleContents contents = DnaSampleItem.getContents(shown);
        if (contents == null) return null;
        Optional<EntityType<?>> type = contents.entityNbt().getString("id").flatMap(EntityType::byString);
        return type.map(t -> mobs.get(t, false)).orElse(null);
    }

    /** A small downward-pointing triangle (5×3 px), centred, its top row at {@code y}. */
    private static void drawDownArrow(GuiGraphicsExtractor g, int y) {
        int cx = WIDTH / 2;
        g.fill(cx - 2, y, cx + 3, y + 1, ARROW_COLOR);
        g.fill(cx - 1, y + 1, cx + 2, y + 2, ARROW_COLOR);
        g.fill(cx, y + 2, cx + 1, y + 3, ARROW_COLOR);
    }
}
