package net.bobofraggins.mobfarmingsupplies.omnihopper.fabric;

import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;

/**
 * Insert-only storage exposed on an Omnidirectional Hopper's INPUT sides, for items or fluids. The
 * hopper holds nothing: whatever is inserted goes straight to an OUTPUT neighbour within the same
 * transaction, so an insert only succeeds if a destination accepts it.
 */
@SuppressWarnings("UnstableApiUsage")
public class OmniHopperInsertStorage<T extends TransferVariant<?>> implements InsertionOnlyStorage<T> {

    private final OmniHopperBlockEntity be;
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Predicate<T> filter;

    public OmniHopperInsertStorage(OmniHopperBlockEntity be, BlockApiLookup<Storage<T>, Direction> lookup,
                                   Predicate<T> filter) {
        this.be = be;
        this.lookup = lookup;
        this.filter = filter;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        if (!filter.test(resource)) return 0;
        return OmniHopperPlatformImpl.routeInsert(be, lookup, resource, maxAmount, transaction);
    }
}
