package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.fluid.FluidStack;
import java.util.List;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

/**
 * Data carrier for the "how do I get XP Juice out of a Tank" guides shown in JEI: the
 * Experience Syringe stores XP withdrawn from a player and empties into a Tank, which can then
 * be scooped with either a bucket ({@link #bucket()}) or a glass bottle ({@link #bottle()}).
 */
public final class XpJuiceGuideRecipe {

    /** One row in the vertical guide: the item to display, its JEI role, and a tooltip hint. */
    public record Step(ItemStack stack, RecipeIngredientRole role, Component tooltip) {}

    private final List<Step> steps;

    private XpJuiceGuideRecipe(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> steps() {
        return steps;
    }

    public static XpJuiceGuideRecipe bucket() {
        return new XpJuiceGuideRecipe(List.of(
                syringeStep(),
                tankStep(),
                new Step(
                        new ItemStack(Registration.XP_JUICE_BUCKET.get()),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.bucket"))));
    }

    public static XpJuiceGuideRecipe bottle() {
        return new XpJuiceGuideRecipe(List.of(
                syringeStep(),
                tankStep(),
                new Step(
                        new ItemStack(Items.EXPERIENCE_BOTTLE),
                        RecipeIngredientRole.OUTPUT,
                        Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.bottle"))));
    }

    private static Step syringeStep() {
        return new Step(
                new ItemStack(Registration.EXPERIENCE_SYRINGE.get()),
                RecipeIngredientRole.INPUT,
                Component.translatable("jei.mobfarmingsupplies.xp_juice_guide.step.syringe"));
    }

    private static Step tankStep() {
        return new Step(
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
