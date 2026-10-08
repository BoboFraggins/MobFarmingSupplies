package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Insert-only energy handler exposed on an Omnidirectional Hopper's (or Einstein-Rosen Bridge's) INPUT sides: energy pushed in
 * goes straight to the OUTPUT neighbours within the same transaction. Reports an effectively
 * unlimited capacity (never full) so generators that check before pushing still push.
 */
public class OmniHopperEnergyHandler implements EnergyHandler {

    private final HopperNode be;

    public OmniHopperEnergyHandler(HopperNode be) {
        this.be = be;
    }

    @Override public long getAmountAsLong() { return 0; }

    @Override public long getCapacityAsLong() { return Integer.MAX_VALUE; }

    @Override
    public int insert(int amount, TransactionContext tx) {
        return OmniHopperPlatformImpl.routeEnergy(be, amount, tx);
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        return 0;
    }
}
