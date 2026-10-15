package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;

/** Moving energy between handlers. */
public final class EnergyHandlerUtil {

    private EnergyHandlerUtil() {}

    /** Moves up to {@code amount} from {@code from} into {@code to}; returns how much moved. */
    public static int move(EnergyHandler from, EnergyHandler to, int amount, TransactionContext tx) {
        int available;
        try (Transaction simulation = Transaction.open(tx)) { // never committed
            available = from.extract(amount, simulation);
        }
        if (available <= 0) return 0;
        try (Transaction step = Transaction.open(tx)) {
            int inserted = to.insert(available, step);
            if (inserted <= 0) return 0;
            if (from.extract(inserted, step) != inserted) return 0;
            step.commit();
            return inserted;
        }
    }
}
