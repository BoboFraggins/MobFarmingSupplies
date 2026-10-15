package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockItem;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Fluid handler for the Tank block item.
 *
 * <p>Reads and writes fluid state via the {@link TankContents} data component.
 * Converts between Architectury's {@link FluidStack} (stored in the component)
 * and NeoForge's fluid stacks using {@link FluidStackHooksForge}.
 * Works on a copy of the stack; call {@link #getContainer()} after mutations to
 * retrieve the updated item.
 */
public class TankItemFluidHandler implements IFluidHandlerItem {

    private final ItemStack container;

    public TankItemFluidHandler(ItemStack stack) {
        this.container = stack.copy();
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    private TankContents contents() {
        TankContents c = container.get(Registration.TANK_CONTENTS.get());
        return c != null ? c : TankContents.EMPTY;
    }

    private static boolean sameFluid(FluidStack stored, net.neoforged.neoforge.fluids.FluidStack incoming) {
        FluidStack arch = FluidStackHooksForge.fromForge(incoming.copyWithAmount(1));
        return stored.isFluidEqual(arch) && stored.isComponentEqual(arch);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public net.neoforged.neoforge.fluids.FluidStack getFluidInTank(int tank) {
        TankContents c = contents();
        if (c.storedFluid().isEmpty() || c.amount() == 0) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        return FluidStackHooksForge.toForge(c.storedFluid().copyWithAmount((int) Math.min(c.amount(), Integer.MAX_VALUE)));
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) Math.min(TankBlockItem.capacityOf(container), Integer.MAX_VALUE);
    }

    @Override
    public boolean isFluidValid(int tank, net.neoforged.neoforge.fluids.FluidStack stack) {
        if (stack.isEmpty()) return false;
        TankContents c = contents();
        return c.storedFluid().isEmpty() || sameFluid(c.storedFluid(), stack);
    }

    @Override
    public int fill(net.neoforged.neoforge.fluids.FluidStack resource, FluidAction action) {
        if (!isFluidValid(0, resource)) return 0;
        TankContents c = contents();
        long space = Math.max(0, TankBlockItem.capacityOf(container) - c.amount());
        int toFill = (int) Math.min(resource.getAmount(), Math.min(space, Integer.MAX_VALUE));
        if (toFill <= 0) return 0;
        if (action.execute()) {
            FluidStack newType = c.storedFluid().isEmpty()
                    ? FluidStackHooksForge.fromForge(resource.copyWithAmount(1))
                    : c.storedFluid();
            container.set(Registration.TANK_CONTENTS.get(),
                    new TankContents(newType, c.amount() + toFill, c.bucketMode()));
        }
        return toFill;
    }

    @Override
    public net.neoforged.neoforge.fluids.FluidStack drain(net.neoforged.neoforge.fluids.FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        TankContents c = contents();
        if (c.storedFluid().isEmpty() || !sameFluid(c.storedFluid(), resource)) {
            return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public net.neoforged.neoforge.fluids.FluidStack drain(int maxDrain, FluidAction action) {
        TankContents c = contents();
        if (c.storedFluid().isEmpty() || maxDrain <= 0) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        int toDrain = (int) Math.min(maxDrain, Math.min(c.amount(), Integer.MAX_VALUE));
        if (toDrain <= 0) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        net.neoforged.neoforge.fluids.FluidStack drained = FluidStackHooksForge.toForge(c.storedFluid().copyWithAmount(toDrain));
        if (action.execute()) {
            container.set(Registration.TANK_CONTENTS.get(),
                    new TankContents(c.storedFluid(), c.amount() - toDrain, c.bucketMode()));
        }
        return drained;
    }
}
