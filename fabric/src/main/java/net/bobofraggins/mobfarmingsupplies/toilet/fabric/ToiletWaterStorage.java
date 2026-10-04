package net.bobofraggins.mobfarmingsupplies.toilet.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Fluid storage on every side of a Toilet: an unlimited supply of water, like Cooking for
 * Blockheads' kitchen sink. Extraction only.
 */
@SuppressWarnings("UnstableApiUsage")
public class ToiletWaterStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {

    public static final ToiletWaterStorage INSTANCE = new ToiletWaterStorage();

    private static final FluidVariant WATER = FluidVariant.of(Fluids.WATER);
    /** Reported amount and capacity: "plenty", kept well clear of overflow when callers add it up. */
    private static final long REPORTED_AMOUNT = Integer.MAX_VALUE;

    private ToiletWaterStorage() {}

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return WATER.equals(resource) ? Math.max(maxAmount, 0) : 0;
    }

    @Override public boolean isResourceBlank() { return false; }

    @Override public FluidVariant getResource() { return WATER; }

    @Override public long getAmount() { return REPORTED_AMOUNT; }

    @Override public long getCapacity() { return REPORTED_AMOUNT; }
}
