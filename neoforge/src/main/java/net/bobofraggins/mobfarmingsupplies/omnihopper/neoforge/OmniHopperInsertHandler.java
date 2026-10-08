package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Insert-only handler exposed on an Omnidirectional Hopper's INPUT sides, for items, fluids or
 * chemicals. The hopper holds nothing: whatever is inserted goes straight to an OUTPUT neighbour
 * within the same transaction, so an insert only succeeds if a destination accepts it.
 */
public class OmniHopperInsertHandler<T extends Resource> implements ResourceHandler<T> {

    private final OmniHopperBlockEntity be;
    private final BlockCapability<ResourceHandler<T>, Direction> capability;
    private final T empty;
    private final Predicate<T> filter;
    private final ToLongFunction<T> capacity;

    /**
     * @param capacity what to report as this handler's capacity for a resource. Never 0 for the
     *                 empty resource: {@code ResourceHandlerUtil.isFull} compares the (always zero)
     *                 amount against it, and NeoForge's hopper won't insert into a "full" handler.
     */
    public OmniHopperInsertHandler(OmniHopperBlockEntity be, BlockCapability<ResourceHandler<T>, Direction> capability,
                                   T empty, Predicate<T> filter, ToLongFunction<T> capacity) {
        this.be = be;
        this.capability = capability;
        this.empty = empty;
        this.filter = filter;
        this.capacity = capacity;
    }

    @Override public int size() { return 1; }

    @Override public T getResource(int index) { return empty; }

    @Override public long getAmountAsLong(int index) { return 0; }

    @Override public long getCapacityAsLong(int index, T resource) { return capacity.applyAsLong(resource); }

    @Override public boolean isValid(int index, T resource) { return !resource.isEmpty() && filter.test(resource); }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext tx) {
        if (!filter.test(resource)) return 0;
        return OmniHopperPlatformImpl.routeInsert(be, capability, resource, amount, tx);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext tx) {
        return 0;
    }
}
