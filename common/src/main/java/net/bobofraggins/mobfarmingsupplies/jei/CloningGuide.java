package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.registry.registries.RegistrySupplier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * JEI guide for cloning: DNA in a powered Clone-O-Matic makes mobs. Shown as a use of DNA Samples,
 * DNA Sample Packs and the Clone-O-Matic: a column of the DNA (cycling through a sample and every
 * pack), the Clone-O-Matic, then a mob that DNA can make. Packs pick at random from their mobs;
 * each shows its first one.
 */
public final class CloningGuide implements IRecipeCategory<CloningGuide.Recipe> {

    public static final RecipeType<Recipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "cloning", Recipe.class);

    /** The DNA shown, one stack per kind. */
    public record Recipe(List<ItemStack> dna) {}

    private static final int WIDTH = 64;
    private static final int SLOT_SIZE = 18;
    private static final int MACHINE_TOP = SLOT_SIZE + 6;
    private static final int MOB_TOP = MACHINE_TOP + SLOT_SIZE + 6;
    private static final int MOB_HEIGHT = 40;
    private static final int HEIGHT = MOB_TOP + MOB_HEIGHT;
    private static final int ARROW_COLOR = 0xFF555555;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.cloning");
    private final IDrawable icon;
    private final MobDisplay mobs = new MobDisplay();

    public CloningGuide(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(Registration.CLONE_O_MATIC_ITEM.get());
    }

    /** The guide's one recipe: a Cow sample, then every pack that has a mob to show. */
    public static Recipe recipe() {
        List<ItemStack> dna = new ArrayList<>();
        dna.add(DnaSamplingGuide.sampleOf(EntityType.COW));
        List<DnaSamplePackItem> packs = new ArrayList<>(List.of(
                Registration.DNA_SAMPLE_COMMON_PASSIVE.get(), Registration.DNA_SAMPLE_COMMON_HOSTILE.get(),
                Registration.DNA_SAMPLE_PASSIVE_RARE.get(), Registration.DNA_SAMPLE_RARE.get(),
                Registration.DNA_SAMPLE_AQUATIC.get(), Registration.DNA_SAMPLE_NETHER.get(),
                Registration.DNA_SAMPLE_BABY.get(), Registration.DNA_SAMPLE_WRONG.get()));
        for (RegistrySupplier<DnaSamplePackItem> booster : Arrays.asList( // null when the mod isn't installed
                ModCompatRegistration.DNA_BOOSTER_AQUACULTURE, ModCompatRegistration.DNA_BOOSTER_AETHER_PASSIVE,
                ModCompatRegistration.DNA_BOOSTER_AETHER_HOSTILE, ModCompatRegistration.DNA_BOOSTER_EVILCRAFT)) {
            if (booster != null) packs.add(booster.get());
        }
        for (DnaSamplePackItem pack : packs) {
            if (pack.exampleMob() != null) dna.add(new ItemStack(pack));
        }
        return new Recipe(dna);
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
                .addItemStacks(recipe.dna())
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.cloning.step.dna")));
        builder.addSlot(RecipeIngredientRole.INPUT, x, MACHINE_TOP)
                .addItemStack(new ItemStack(Registration.CLONE_O_MATIC_ITEM.get()))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.cloning.step.machine")));
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        drawDownArrow(graphics, SLOT_SIZE + 1);
        drawDownArrow(graphics, MACHINE_TOP + SLOT_SIZE + 1);
        LivingEntity mob = shownMob(slots);
        if (mob != null) MobDisplay.draw(graphics, mob, 0, MOB_TOP, WIDTH, MOB_TOP + MOB_HEIGHT, mouseX, mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Recipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseY < MOB_TOP || mouseY >= MOB_TOP + MOB_HEIGHT) return;
        LivingEntity mob = shownMob(slots);
        if (mob != null) tooltip.add(mob.getType().getDescription());
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.cloning.step.mob"));
    }

    /** The mob the DNA showing right now makes, or null if it can't be drawn. */
    @Nullable
    private LivingEntity shownMob(IRecipeSlotsView slots) {
        ItemStack shown = slots.getSlotViews(RecipeIngredientRole.INPUT).stream().findFirst()
                .flatMap(view -> view.getDisplayedItemStack()).orElse(ItemStack.EMPTY);
        if (shown.getItem() instanceof DnaSamplePackItem pack) {
            EntityType<?> type = pack.exampleMob();
            return type == null ? null : mobs.get(type, pack.forcesBaby());
        }
        DnaSampleContents contents = DnaSampleItem.getContents(shown);
        if (contents == null) return null;
        return EntityType.byString(contents.entityNbt().getString("id"))
                .map(type -> mobs.get(type, false)).orElse(null);
    }

    /** A small downward-pointing triangle (5×3 px), centred, its top row at {@code y}. */
    private static void drawDownArrow(GuiGraphics g, int y) {
        int cx = WIDTH / 2;
        g.fill(cx - 2, y, cx + 3, y + 1, ARROW_COLOR);
        g.fill(cx - 1, y + 1, cx + 2, y + 2, ARROW_COLOR);
        g.fill(cx, y + 2, cx + 1, y + 3, ARROW_COLOR);
    }
}
