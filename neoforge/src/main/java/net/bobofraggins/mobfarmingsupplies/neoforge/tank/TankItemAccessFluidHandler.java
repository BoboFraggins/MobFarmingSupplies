package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.neoforge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockItem;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * The Tank item's fluid capability. Changes go back to the real item through its
 * {@link ItemAccess} (as {@code ExperienceSyringeFluidHandler} does); {@link TankItemFluidHandler}
 * only edits a copy, so fluid poured into a held tank through it was lost.
 */
public class TankItemAccessFluidHandler extends ItemAccessResourceHandler<FluidResource> {

    public TankItemAccessFluidHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    private static TankContents contents(ItemResource resource) {
        return resource.toStack(1).getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        TankContents c = contents(accessResource);
        if (c.isEmpty()) return FluidResource.EMPTY;
        return FluidResource.of(FluidStackHooksForge.toForge(c.storedFluid().copyWithAmount(1)));
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        return (int) Math.min(contents(accessResource).amount(), Integer.MAX_VALUE);
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        ItemStack updated = accessResource.toStack(1);
        boolean bucketMode = contents(accessResource).bucketMode();
        updated.set(Registration.TANK_CONTENTS.get(), newAmount <= 0
                ? new TankContents(FluidStack.empty(), 0, bucketMode)
                : new TankContents(FluidStackHooksForge.fromForge(newResource.toStack(1)), newAmount, bucketMode));
        return ItemResource.of(updated);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return (int) Math.min(TankBlockItem.capacityOf(itemAccess.getResource().toStack(1)), Integer.MAX_VALUE);
    }

    /** A tank holding fluid only takes more of the same; an empty one takes anything. */
    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) return false;
        TankContents c = contents(itemAccess.getResource());
        if (c.isEmpty()) return true;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        return c.storedFluid().isFluidEqual(incoming) && c.storedFluid().isComponentEqual(incoming);
    }
}
