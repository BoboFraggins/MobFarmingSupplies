package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;

/** Moving resources between handlers. */
public final class ResourceHandlerUtil {

    private ResourceHandlerUtil() {}

    /** Whether every slot of {@code handler} is at its capacity. */
    public static <T extends Resource> boolean isFull(ResourceHandler<T> handler) {
        for (int slot = 0, size = handler.size(); slot < size; slot++) {
            if (handler.getAmountAsLong(slot) < handler.getCapacityAsLong(slot, handler.getResource(slot))) return false;
        }
        return true;
    }

    /**
     * Inserts into slots already holding {@code resource} first, then into any others. Returns how
     * much went in.
     */
    public static <T extends Resource> int insertStacking(ResourceHandler<T> handler, T resource, int amount,
                                                          TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        int inserted = 0;
        int size = handler.size();
        for (int slot = 0; slot < size && inserted < amount; slot++) {
            if (resource.equals(handler.getResource(slot))) {
                inserted += handler.insert(slot, resource, amount - inserted, tx);
            }
        }
        for (int slot = 0; slot < size && inserted < amount; slot++) {
            if (!resource.equals(handler.getResource(slot))) {
                inserted += handler.insert(slot, resource, amount - inserted, tx);
            }
        }
        return inserted;
    }

    /** Moves up to {@code amount} of what passes {@code filter} from {@code from} into {@code to}. */
    public static <T extends Resource> int move(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter,
                                                int amount, TransactionContext tx) {
        return move(from, to, filter, amount, tx, false);
    }

    /** As {@link #move}, filling slots already holding a resource before empty ones. */
    public static <T extends Resource> int moveStacking(ResourceHandler<T> from, ResourceHandler<T> to,
                                                        Predicate<T> filter, int amount, TransactionContext tx) {
        return move(from, to, filter, amount, tx, true);
    }

    private static <T extends Resource> int move(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter,
                                                 int amount, TransactionContext tx, boolean stacking) {
        int moved = 0;
        for (int slot = 0, size = from.size(); slot < size && moved < amount; slot++) {
            T resource = from.getResource(slot);
            if (resource.isEmpty() || !filter.test(resource)) continue;
            int available;
            try (Transaction simulation = Transaction.open(tx)) { // never committed
                available = from.extract(slot, resource, amount - moved, simulation);
            }
            if (available <= 0) continue;
            try (Transaction step = Transaction.open(tx)) {
                int inserted = stacking
                        ? insertStacking(to, resource, available, step)
                        : to.insert(resource, available, step);
                if (inserted <= 0) continue;
                if (from.extract(slot, resource, inserted, step) == inserted) {
                    step.commit();
                    moved += inserted;
                }
            }
        }
        return moved;
    }
}
