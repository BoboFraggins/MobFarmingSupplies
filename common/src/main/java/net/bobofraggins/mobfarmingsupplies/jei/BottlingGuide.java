package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI guide for "how do I get a Bottle o' Enchanting": XP stored by the Experience Syringe goes
 * into a Tank or an Experience Pylon, and a glass bottle used on it is filled with experience.
 * One page with the Tank, one with the Pylon.
 */
public final class BottlingGuide {

    public static final RecipeType<GuideRecipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "bottling_guide", GuideRecipe.class);

    private BottlingGuide() {}

    public static DialogGuideCategory category(IGuiHelper helper) {
        return new DialogGuideCategory(RECIPE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.bottling_guide"),
                helper.createDrawableItemLike(Items.EXPERIENCE_BOTTLE), helper);
    }

    /** The Tank page, then the Experience Pylon page. */
    public static List<GuideRecipe> recipes() {
        return List.of(
                page(XpJuiceGuide.tankOfXpJuice(), "jei.mobfarmingsupplies.bottling_guide.step.tank",
                        "jei.mobfarmingsupplies.bottling_guide.step.tank_bottle"),
                page(new ItemStack(Registration.EXPERIENCE_PYLON_ITEM.get()),
                        "jei.mobfarmingsupplies.xp_juice_guide.step.pylon",
                        "jei.mobfarmingsupplies.bottling_guide.step.pylon_bottle"));
    }

    private static GuideRecipe page(ItemStack store, String storeHint, String bottleHint) {
        return GuideRecipe.column(
                new GuideRecipe.Step(
                        new ItemStack(Registration.EXPERIENCE_SYRINGE.get()),
                        RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.syringe")),
                new GuideRecipe.Step(store, RecipeIngredientRole.CATALYST, Component.translatable(storeHint)),
                new GuideRecipe.Step(
                        new ItemStack(Items.EXPERIENCE_BOTTLE),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable(bottleHint)))
                .titled(store.getHoverName());
    }
}
