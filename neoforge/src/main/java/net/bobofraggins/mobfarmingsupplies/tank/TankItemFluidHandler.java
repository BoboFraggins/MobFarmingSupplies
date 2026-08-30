package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.neoforge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * {@link ResourceHandler}{@code <FluidResource>} for the Tank block item.
 *
 * <p>Reads and writes fluid state via the {@link TankContents} data component.
 * Converts between Architectury's {@link FluidStack} (stored in the component)
 * and NeoForge's {@link FluidResource} using {@link FluidStackHooksForge}.
 * Works on a copy of the stack; call {@link #getContainer()} after mutations to
 * retrieve the updated item.
 */
public class TankItemFluidHandler implements ResourceHandler<FluidResource> {

    private final ItemStack container;

    public TankItemFluidHandler(ItemStack stack) {
        this.container = stack.copy();
    }

    public ItemStack getContainer() {
        return container;
    }

    private TankContents contents() {
        TankContents c = container.get(Registration.TANK_CONTENTS.get());
        return c != null ? c : TankContents.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        TankContents c = contents();
        if (c.storedFluid().isEmpty() || c.amount() == 0) return FluidResource.EMPTY;
        return FluidResource.of(FluidStackHooksForge.toForge(c.storedFluid().copyWithAmount(1)));
    }

    @Override
    public long getAmountAsLong(int index) {
        return contents().amount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return TankBlockEntity.CAPACITY;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) return false;
        TankContents c = contents();
        if (c.storedFluid().isEmpty()) return true;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        return c.storedFluid().isFluidEqual(incoming) && c.storedFluid().isComponentEqual(incoming);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (!isValid(index, resource) || amount <= 0) return 0;
        TankContents c = contents();
        long space = TankBlockEntity.CAPACITY - c.amount();
        int toFill = (int) Math.min(amount, Math.min(space, Integer.MAX_VALUE));
        if (toFill <= 0) return 0;
        FluidStack newType = c.storedFluid().isEmpty()
                ? FluidStackHooksForge.fromForge(resource.toStack(1)).copyWithAmount(1)
                : c.storedFluid();
        container.set(Registration.TANK_CONTENTS.get(),
                new TankContents(newType, c.amount() + toFill, c.bucketMode()));
        return toFill;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        TankContents c = contents();
        if (c.storedFluid().isEmpty()) return 0;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        if (!c.storedFluid().isFluidEqual(incoming) || !c.storedFluid().isComponentEqual(incoming)) return 0;
        int toDrain = (int) Math.min(amount, Math.min(c.amount(), Integer.MAX_VALUE));
        if (toDrain <= 0) return 0;
        container.set(Registration.TANK_CONTENTS.get(),
                new TankContents(c.storedFluid(), c.amount() - toDrain, c.bucketMode()));
        return toDrain;
    }
}
