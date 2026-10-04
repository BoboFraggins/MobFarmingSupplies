package net.bobofraggins.mobfarmingsupplies.neoforge.toilet;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Fluid handler on every side of a Toilet: an unlimited supply of water, like Cooking for
 * Blockheads' kitchen sink. Extraction only.
 */
public class ToiletFluidHandler implements ResourceHandler<FluidResource> {

    public static final ToiletFluidHandler INSTANCE = new ToiletFluidHandler();

    private static final FluidResource WATER = FluidResource.of(Fluids.WATER);

    private ToiletFluidHandler() {}

    @Override public int size() { return 1; }

    @Override public FluidResource getResource(int index) { return WATER; }

    @Override public long getAmountAsLong(int index) { return Integer.MAX_VALUE; }

    @Override public long getCapacityAsLong(int index, FluidResource resource) { return Integer.MAX_VALUE; }

    @Override public boolean isValid(int index, FluidResource resource) { return WATER.equals(resource); }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        return WATER.equals(resource) ? Math.max(amount, 0) : 0;
    }
}
