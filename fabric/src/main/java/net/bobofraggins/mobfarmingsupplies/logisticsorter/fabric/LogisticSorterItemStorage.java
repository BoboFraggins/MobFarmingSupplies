package net.bobofraggins.mobfarmingsupplies.logisticsorter.fabric;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Insert-only item storage exposed on a Logistic Sorter's INPUT sides. The sorter holds no
 * items: whatever is inserted goes straight to a MATCH / NO_MATCH neighbour within the same
 * transaction, so an insert only succeeds if a destination accepts it.
 */
@SuppressWarnings("UnstableApiUsage")
public class LogisticSorterItemStorage implements InsertionOnlyStorage<ItemVariant> {

    private final LogisticSorterBlockEntity be;

    public LogisticSorterItemStorage(LogisticSorterBlockEntity be) {
        this.be = be;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return LogisticSorterPlatformImpl.routeInsert(be, resource, maxAmount, transaction);
    }
}
