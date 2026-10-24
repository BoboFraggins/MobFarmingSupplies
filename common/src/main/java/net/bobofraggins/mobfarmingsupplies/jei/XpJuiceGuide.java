package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.fluid.FluidStack;
import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI guide for "how do I get an XP Juice Bucket": the Experience Syringe stores XP withdrawn
 * from a player, empties into a Tank, and a Tank full of XP Juice can be scooped with a bucket.
 * A second page does the same with an Experience Pylon in place of the Tank.
 */
public final class XpJuiceGuide {

    public static final RecipeType<GuideRecipe> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "xp_juice_guide", GuideRecipe.class);

    private XpJuiceGuide() {}

    public static DialogGuideCategory category(IGuiHelper helper) {
        return new DialogGuideCategory(RECIPE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.xp_juice_guide"),
                helper.createDrawableItemLike(Registration.XP_JUICE_BUCKET.get()), helper);
    }

    /** The Tank page, then the Experience Pylon page. */
    public static List<GuideRecipe> recipes() {
        return List.of(recipe(), pylonRecipe());
    }

    public static GuideRecipe recipe() {
        return GuideRecipe.column(
                new GuideRecipe.Step(
                        new ItemStack(Registration.EXPERIENCE_SYRINGE.get()),
                        RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.syringe")),
                new GuideRecipe.Step(
                        tankOfXpJuice(),
                        RecipeIngredientRole.CRAFTING_STATION,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.tank")),
                new GuideRecipe.Step(
                        new ItemStack(Registration.XP_JUICE_BUCKET.get()),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.bucket")))
                .titled(new ItemStack(Registration.TANK_ITEM.get()).getHoverName());
    }

    private static GuideRecipe pylonRecipe() {
        return GuideRecipe.column(
                new GuideRecipe.Step(
                        new ItemStack(Registration.EXPERIENCE_SYRINGE.get()),
                        RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.syringe")),
                new GuideRecipe.Step(
                        new ItemStack(Registration.EXPERIENCE_PYLON_ITEM.get()),
                        RecipeIngredientRole.CRAFTING_STATION,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.pylon")),
                new GuideRecipe.Step(
                        new ItemStack(Registration.XP_JUICE_BUCKET.get()),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.pylon_bucket")))
                .titled(new ItemStack(Registration.EXPERIENCE_PYLON_ITEM.get()).getHoverName());
    }

    /** A Tank item stack shown full of XP Juice, matching the block form's own fluid rendering. */
    static ItemStack tankOfXpJuice() {
        ItemStack stack = new ItemStack(Registration.TANK_ITEM.get());
        FluidStack fluid = FluidStack.create(Registration.XP_JUICE_SOURCE.get(), 1);
        stack.set(Registration.TANK_CONTENTS.get(), new TankContents(fluid, TankTier.BASIC.capacity()));
        return stack;
    }
}
