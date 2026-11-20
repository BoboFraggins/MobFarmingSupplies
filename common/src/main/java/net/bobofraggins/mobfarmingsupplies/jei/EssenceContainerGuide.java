package net.bobofraggins.mobfarmingsupplies.jei;

import dev.architectury.fluid.FluidStack;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * JEI guides for "how do I get a bucket (or bottle) of an essence", after {@link XpJuiceGuide}:
 * an Essence Hopper with that essence's filter pushes what it collects into a Tank, and a bucket
 * or glass bottle used on the Tank scoops it out. The hopper has no fluid inventory of its own to
 * scoop from. One page per essence, so looking up one bucket finds just its own.
 */
public final class EssenceContainerGuide {

    public static final RecipeType<GuideRecipe> BUCKET_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "essence_bucket_guide", GuideRecipe.class);
    public static final RecipeType<GuideRecipe> BOTTLE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "essence_bottle_guide", GuideRecipe.class);

    private EssenceContainerGuide() {}

    public static DialogGuideCategory bucketCategory(IGuiHelper helper) {
        return new DialogGuideCategory(BUCKET_TYPE,
                Component.translatable("jei.mobfarmingsupplies.essence_bucket_guide"),
                helper.createDrawableItemLike(Registration.ESSENCE_BUCKETS.get(Essence.values()[0]).get()), helper);
    }

    public static DialogGuideCategory bottleCategory(IGuiHelper helper) {
        return new DialogGuideCategory(BOTTLE_TYPE,
                Component.translatable("jei.mobfarmingsupplies.essence_bottle_guide"),
                helper.createDrawableItemLike(Registration.ESSENCE_BOTTLES.get(Essence.values()[0]).get()), helper);
    }

    public static List<GuideRecipe> bucketRecipes() {
        return Arrays.stream(Essence.values()).map(essence -> page(essence,
                new ItemStack(Registration.ESSENCE_BUCKETS.get(essence).get()),
                "jei.mobfarmingsupplies.essence_bucket_guide.step.bucket")).toList();
    }

    public static List<GuideRecipe> bottleRecipes() {
        return Arrays.stream(Essence.values()).map(essence -> page(essence,
                new ItemStack(Registration.ESSENCE_BOTTLES.get(essence).get()),
                "jei.mobfarmingsupplies.essence_bottle_guide.step.bottle")).toList();
    }

    private static GuideRecipe page(Essence essence, ItemStack result, String resultHint) {
        ItemStack tank = tankOf(essence);
        return new GuideRecipe(List.of(
                new GuideRecipe.Row(List.of(
                        new GuideRecipe.Step(new ItemStack(Registration.ESSENCE_FILTERS.get(essence).get()),
                                RecipeIngredientRole.INPUT,
                                Component.translatable("jei.mobfarmingsupplies.essence_extraction.step.filter")),
                        new GuideRecipe.Step(new ItemStack(Registration.ESSENCE_HOPPER_ITEM.get()),
                                RecipeIngredientRole.CRAFTING_STATION,
                                Component.translatable("jei.mobfarmingsupplies.essence_container_guide.step.hopper")))),
                new GuideRecipe.Row(List.of(
                        new GuideRecipe.Step(tank, RecipeIngredientRole.CRAFTING_STATION,
                                Component.translatable("jei.mobfarmingsupplies.essence_container_guide.step.tank")))),
                new GuideRecipe.Row(List.of(
                        new GuideRecipe.Step(result, RecipeIngredientRole.OUTPUT, Component.translatable(resultHint))))))
                .titled(new ItemStack(Registration.TANK_ITEM.get()).getHoverName());
    }

    /** A Tank item shown full of the essence. */
    private static ItemStack tankOf(Essence essence) {
        ItemStack stack = new ItemStack(Registration.TANK_ITEM.get());
        FluidStack fluid = FluidStack.create(Registration.ESSENCE_SOURCES.get(essence).get(), 1);
        stack.set(Registration.TANK_CONTENTS.get(), new TankContents(fluid, TankTier.BASIC.capacity()));
        return stack;
    }
}
