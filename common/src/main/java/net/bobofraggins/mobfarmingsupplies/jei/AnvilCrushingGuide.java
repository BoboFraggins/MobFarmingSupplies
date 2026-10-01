package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI guide for anvil crushing: a falling anvil landing on Nether Quartz items turns them into
 * Silicon Clumps (see {@link net.bobofraggins.mobfarmingsupplies.crushing.AnvilCrushing}).
 *
 * <p>Built client-side rather than read from the {@code anvil_crushing} recipes: since MC 1.21.2
 * custom recipe types aren't synced to clients, so JEI can't see them without extra per-loader
 * sync code. Keep this in step with {@code recipe/silicon_clump_from_anvil_crushing.json}.
 */
public final class AnvilCrushingGuide {

    public static final IRecipeType<GuideRecipe> RECIPE_TYPE =
            IRecipeType.create(MobFarmingSuppliesCommon.MODID, "anvil_crushing", GuideRecipe.class);

    private AnvilCrushingGuide() {}

    public static VerticalGuideCategory category(IGuiHelper helper) {
        return new VerticalGuideCategory(RECIPE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.anvil_crushing"),
                helper.createDrawableItemLike(Items.ANVIL), 3);
    }

    public static GuideRecipe recipe() {
        return new GuideRecipe(List.of(
                new GuideRecipe.Step(
                        List.of(new ItemStack(Items.ANVIL), new ItemStack(Items.CHIPPED_ANVIL),
                                new ItemStack(Items.DAMAGED_ANVIL)),
                        RecipeIngredientRole.CRAFTING_STATION,
                        Component.translatable("jei.mobfarmingsupplies.anvil_crushing.step.anvil")),
                new GuideRecipe.Step(
                        new ItemStack(Items.QUARTZ),
                        RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.anvil_crushing.step.quartz")),
                new GuideRecipe.Step(
                        new ItemStack(Registration.SILICON_CLUMP.get()),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.anvil_crushing.step.clump"))));
    }
}
