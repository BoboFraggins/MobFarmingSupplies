package net.bobofraggins.mobfarmingsupplies.forge.transfer.api.fluid;

import java.util.Objects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Forge 1.20.1 fluid and item stack helpers with the names NeoForge 1.21's stacks have. */
public final class ForgeFluids {

    private ForgeFluids() {}

    public static FluidStack copyWithAmount(FluidStack stack, int amount) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack, amount);
    }

    /** Same fluid and same NBT (NeoForge's {@code isSameFluidSameComponents}). */
    public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
        return a.isFluidEqual(b);
    }

    public static int hashFluidAndComponents(FluidStack stack) {
        return stack.isEmpty() ? 0 : 31 * stack.getFluid().hashCode() + Objects.hashCode(stack.getTag());
    }

    public static boolean is(FluidStack stack, TagKey<Fluid> tag) {
        return !stack.isEmpty() && stack.getFluid().is(tag);
    }

    public static int hashItemAndComponents(ItemStack stack) {
        return stack.isEmpty() ? 0 : 31 * stack.getItem().hashCode() + Objects.hashCode(stack.getTag());
    }
}
