package net.bobofraggins.mobfarmingsupplies.logisticsorter.neoforge;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Insert-only item handler exposed on a Logistic Sorter's INPUT sides. The sorter holds no
 * items: whatever is inserted goes straight to a MATCH / NO_MATCH neighbour within the same
 * transaction, so an insert only succeeds if a destination accepts it.
 */
public class LogisticSorterItemHandler implements ResourceHandler<ItemResource> {

    private final LogisticSorterBlockEntity be;

    public LogisticSorterItemHandler(LogisticSorterBlockEntity be) {
        this.be = be;
    }

    @Override public int size() { return 1; }

    @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }

    @Override public long getAmountAsLong(int index) { return 0; }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? 0 : resource.toStack(1).getMaxStackSize();
    }

    @Override public boolean isValid(int index, ItemResource resource) { return !resource.isEmpty(); }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        return LogisticSorterPlatformImpl.routeInsert(be, resource, amount, tx);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
