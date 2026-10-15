package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;

/**
 * Holds resources in indexed slots and moves them in and out inside transactions. Amounts are
 * items or millibuckets.
 */
public interface ResourceHandler<T extends Resource> {

    int size();

    /** What slot {@code index} holds; the empty resource if nothing. */
    T getResource(int index);

    long getAmountAsLong(int index);

    /** How much of {@code resource} slot {@code index} could hold in all. */
    long getCapacityAsLong(int index, T resource);

    /** Whether slot {@code index} could ever take {@code resource}. */
    boolean isValid(int index, T resource);

    /** Puts up to {@code amount} of {@code resource} into slot {@code index}; returns how much went in. */
    int insert(int index, T resource, int amount, TransactionContext tx);

    /** Takes up to {@code amount} of {@code resource} from slot {@code index}; returns how much came out. */
    int extract(int index, T resource, int amount, TransactionContext tx);

    /** Puts up to {@code amount} of {@code resource} into any slots, in order. */
    default int insert(T resource, int amount, TransactionContext tx) {
        int inserted = 0;
        for (int i = 0, size = size(); i < size && inserted < amount; i++) {
            inserted += insert(i, resource, amount - inserted, tx);
        }
        return inserted;
    }

    /** Takes up to {@code amount} of {@code resource} from any slots, in order. */
    default int extract(T resource, int amount, TransactionContext tx) {
        int extracted = 0;
        for (int i = 0, size = size(); i < size && extracted < amount; i++) {
            if (resource.equals(getResource(i))) extracted += extract(i, resource, amount - extracted, tx);
        }
        return extracted;
    }

    @SuppressWarnings("unchecked")
    static <T extends Resource> Class<ResourceHandler<T>> asClass() {
        return (Class<ResourceHandler<T>>) (Class<?>) ResourceHandler.class;
    }
}
