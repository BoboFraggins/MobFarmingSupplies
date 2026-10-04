package net.bobofraggins.mobfarmingsupplies.neoforge.toilet;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Item handler on every side of a Toilet: accepts any item and voids it. Holds nothing, so
 * nothing can be extracted.
 */
public class ToiletItemHandler implements ResourceHandler<ItemResource> {

    public static final ToiletItemHandler INSTANCE = new ToiletItemHandler();

    private ToiletItemHandler() {}

    @Override public int size() { return 1; }

    @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }

    @Override public long getAmountAsLong(int index) { return 0; }

    /**
     * Unlimited, including for {@link ItemResource#EMPTY}: {@code ResourceHandlerUtil.isFull} compares
     * the (always zero) amount against the capacity of the slot's current, empty resource, and
     * NeoForge's hopper hook won't even try to insert into a handler it considers full.
     */
    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return Integer.MAX_VALUE;
    }

    @Override public boolean isValid(int index, ItemResource resource) { return !resource.isEmpty(); }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        return resource.isEmpty() ? 0 : Math.max(amount, 0);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
