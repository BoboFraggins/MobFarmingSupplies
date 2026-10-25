package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.registry.registries.RegistrySupplier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleContents;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * JEI guide for cloning: DNA in a powered Clone-O-Matic makes mobs. Shown as a use of DNA Samples,
 * DNA Sample Packs and the Clone-O-Matic, in a dialog titled with the Clone-O-Matic: the DNA
 * (cycling through a sample and every pack), the Clone-O-Matic, then a mob that DNA can make.
 * Packs pick at random from their mobs; each shows its first one.
 */
public final class CloningGuide extends DialogGuideCategory {

    public static final IRecipeType<GuideRecipe> RECIPE_TYPE =
            IRecipeType.create(MobFarmingSuppliesCommon.MODID, "cloning", GuideRecipe.class);

    private static final int MOB_WIDTH = 40;
    private static final int MOB_HEIGHT = 40;

    private final MobDisplay mobs = new MobDisplay();

    public CloningGuide(IGuiHelper helper) {
        super(RECIPE_TYPE, Component.translatable("jei.mobfarmingsupplies.cloning"),
                helper.createDrawableItemLike(Registration.CLONE_O_MATIC_ITEM.get()), helper, MOB_WIDTH, MOB_HEIGHT);
    }

    /** The guide's one recipe: a Cow sample, then every pack that has a mob to show. */
    public static GuideRecipe recipe() {
        List<ItemStack> dna = new ArrayList<>();
        dna.add(DnaSamplingGuide.sampleOf(EntityTypes.COW));
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
        return GuideRecipe.column(
                new GuideRecipe.Step(dna, RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.cloning.step.dna")),
                new GuideRecipe.Step(new ItemStack(Registration.CLONE_O_MATIC_ITEM.get()), RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.cloning.step.machine")))
                .titled(new ItemStack(Registration.CLONE_O_MATIC_ITEM.get()).getHoverName());
    }

    @Override
    protected void drawTrailing(GuideRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, int x, int y,
                                double mouseX, double mouseY) {
        LivingEntity mob = shownMob(slots);
        int top = trailingY(MOB_HEIGHT);
        if (mob != null) MobDisplay.draw(g, mob, x, top, x + MOB_WIDTH, top + MOB_HEIGHT, mouseX, mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, GuideRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        int x = trailingX(recipe);
        int top = trailingY(MOB_HEIGHT);
        if (mouseX < x || mouseX >= x + MOB_WIDTH || mouseY < top || mouseY >= top + MOB_HEIGHT) return;
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
        return contents.entityNbt().getString("id").map(Identifier::tryParse)
                .flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional)
                .map(type -> mobs.get(type, false)).orElse(null);
    }
}
