package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;

/** Stores energy (FE) and moves it in and out inside transactions. */
public interface EnergyHandler {

    long getAmountAsLong();

    long getCapacityAsLong();

    /** Takes in up to {@code amount}; returns how much went in. */
    int insert(int amount, TransactionContext tx);

    /** Gives out up to {@code amount}; returns how much came out. */
    int extract(int amount, TransactionContext tx);
}
