package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** A fluid with its components, without an amount. Immutable. */
public final class FluidResource implements Resource {

    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    private final FluidStack prototype;

    private FluidResource(FluidStack prototype) {
        this.prototype = prototype;
    }

    public static FluidResource of(FluidStack stack) {
        return stack.isEmpty() ? EMPTY : new FluidResource(stack.copyWithAmount(1));
    }

    public static FluidResource of(Fluid fluid) {
        return of(new FluidStack(fluid, 1));
    }

    @Override
    public boolean isEmpty() {
        return prototype.isEmpty();
    }

    public Fluid getFluid() {
        return prototype.getFluid();
    }

    /** A new stack of {@code amount} of this fluid. */
    public FluidStack toStack(int amount) {
        return prototype.copyWithAmount(amount);
    }

    /** Whether {@code stack} is this fluid with these components. */
    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(prototype, stack);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FluidResource other && FluidStack.isSameFluidSameComponents(prototype, other.prototype);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(prototype);
    }

    @Override
    public String toString() {
        return "FluidResource[" + prototype + "]";
    }
}
