package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.fluid.FluidStack;
import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI guides for "how do I get XP Juice out of a Tank": the Experience Syringe stores XP
 * withdrawn from a player and empties into a Tank, which can then be scooped with either a
 * bucket ({@link #bucket()}) or a glass bottle ({@link #bottle()}).
 */
public final class XpJuiceGuide {

    public static final IRecipeType<GuideRecipe> RECIPE_TYPE =
            IRecipeType.create(MobFarmingSuppliesCommon.MODID, "xp_juice_guide", GuideRecipe.class);

    private XpJuiceGuide() {}

    public static VerticalGuideCategory category(IGuiHelper helper) {
        return new VerticalGuideCategory(RECIPE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.xp_juice_guide"),
                helper.createDrawableItemLike(Registration.XP_JUICE_BUCKET.get()), 3);
    }

    public static GuideRecipe bucket() {
        return new GuideRecipe(List.of(
                syringeStep(),
                tankStep(),
                new GuideRecipe.Step(
                        new ItemStack(Registration.XP_JUICE_BUCKET.get()),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.bucket"))));
    }

    public static GuideRecipe bottle() {
        return new GuideRecipe(List.of(
                syringeStep(),
                tankStep(),
                new GuideRecipe.Step(
                        new ItemStack(Items.EXPERIENCE_BOTTLE),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.bottle"))));
    }

    private static GuideRecipe.Step syringeStep() {
        return new GuideRecipe.Step(
                new ItemStack(Registration.EXPERIENCE_SYRINGE.get()),
                RecipeIngredientRole.INPUT,
                Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.syringe"));
    }

    private static GuideRecipe.Step tankStep() {
        return new GuideRecipe.Step(
                tankOfXpJuice(),
                RecipeIngredientRole.CRAFTING_STATION,
                Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.tank"));
    }

    /** A Tank item stack shown full of XP Juice, matching the block form's own fluid rendering. */
    private static ItemStack tankOfXpJuice() {
        ItemStack stack = new ItemStack(Registration.TANK_ITEM.get());
        FluidStack fluid = FluidStack.create(Registration.XP_JUICE_SOURCE.get(), 1);
        stack.set(Registration.TANK_CONTENTS.get(), new TankContents(fluid, TankBlockEntity.CAPACITY));
        return stack;
    }
}
