package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeads;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI guide for mob heads: a Mob Harvester with a Beheading Upgrade makes what it kills drop its
 * head. Shown as the recipe for every head (vanilla's, players' and this mod's Mob Heads, cycling)
 * and as a use of the Harvester and the upgrade.
 */
public final class BeheadingGuide {

    public static final RecipeType<GuideRecipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "beheading", GuideRecipe.class);

    private BeheadingGuide() {}

    public static VerticalGuideCategory category(IGuiHelper helper) {
        return new VerticalGuideCategory(RECIPE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.beheading"),
                helper.createDrawableItemLike(Items.CREEPER_HEAD), 3, 2);
    }

    public static GuideRecipe recipe() {
        return new GuideRecipe(List.of(
                new GuideRecipe.Row(List.of(
                        new GuideRecipe.Step(
                                new ItemStack(Registration.MOB_HARVESTER_ITEM.get()),
                                RecipeIngredientRole.INPUT,
                                Component.translatable("jei.mobfarmingsupplies.beheading.step.harvester")),
                        new GuideRecipe.Step(
                                new ItemStack(Registration.HARVESTER_UPGRADE_BEHEADING.get()),
                                RecipeIngredientRole.INPUT,
                                Component.translatable("jei.mobfarmingsupplies.beheading.step.upgrade")))),
                new GuideRecipe.Row(List.of(
                        new GuideRecipe.Step(
                                MobHeads.allHeads(),
                                RecipeIngredientRole.OUTPUT,
                                Component.translatable("jei.mobfarmingsupplies.beheading.step.head"))))));
    }
}
