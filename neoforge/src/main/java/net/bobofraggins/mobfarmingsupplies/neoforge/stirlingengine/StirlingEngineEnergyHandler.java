package net.bobofraggins.mobfarmingsupplies.neoforge.stirlingengine;

import net.bobofraggins.mobfarmingsupplies.stirlingengine.StirlingEngineBlockEntity;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The Stirling Engine's buffer as a NeoForge energy handler: neighbours can pull from it but not
 * fill it. Extractions are undone if their transaction is.
 */
public class StirlingEngineEnergyHandler implements EnergyHandler {

    private final StirlingEngineBlockEntity be;

    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override
        protected Long createSnapshot() {
            return be.energy();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            be.setEnergy(snapshot);
        }
    };

    public StirlingEngineEnergyHandler(StirlingEngineBlockEntity be) {
        this.be = be;
    }

    /** The handler for {@code be}, made once so every caller shares one journal. */
    public static StirlingEngineEnergyHandler of(StirlingEngineBlockEntity be) {
        if (be.energyHandler instanceof StirlingEngineEnergyHandler existing) return existing;
        StirlingEngineEnergyHandler handler = new StirlingEngineEnergyHandler(be);
        be.energyHandler = handler;
        return handler;
    }

    @Override
    public long getAmountAsLong() {
        return be.energy();
    }

    @Override
    public long getCapacityAsLong() {
        return be.capacity();
    }

    @Override
    public int insert(int amount, TransactionContext tx) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        int taken = (int) Math.min(amount, be.energy());
        if (taken <= 0) return 0;
        journal.updateSnapshots(tx);
        be.setEnergy(be.energy() - taken);
        return taken;
    }
}
