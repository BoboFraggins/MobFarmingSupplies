package net.bobofraggins.mobfarmingsupplies.toilet.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Item storage on every side of a Toilet: accepts any item and voids it. Holds nothing, so
 * nothing can be extracted.
 */
@SuppressWarnings("UnstableApiUsage")
public class ToiletItemStorage implements InsertionOnlyStorage<ItemVariant> {

    public static final ToiletItemStorage INSTANCE = new ToiletItemStorage();

    private ToiletItemStorage() {}

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return resource.isBlank() ? 0 : Math.max(maxAmount, 0);
    }
}
