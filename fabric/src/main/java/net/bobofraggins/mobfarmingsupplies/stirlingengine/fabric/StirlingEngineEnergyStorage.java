package net.bobofraggins.mobfarmingsupplies.stirlingengine.fabric;

import net.bobofraggins.mobfarmingsupplies.stirlingengine.StirlingEngineBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * The Stirling Engine's buffer as a Team Reborn Energy storage: neighbours can pull from it but
 * not fill it. Extractions are undone if their transaction is.
 */
public class StirlingEngineEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {

    private final StirlingEngineBlockEntity be;

    private StirlingEngineEnergyStorage(StirlingEngineBlockEntity be) {
        this.be = be;
    }

    /** The storage for {@code be}, made once so every caller shares one snapshot history. */
    public static StirlingEngineEnergyStorage of(StirlingEngineBlockEntity be) {
        if (be.energyHandler instanceof StirlingEngineEnergyStorage existing) return existing;
        StirlingEngineEnergyStorage storage = new StirlingEngineEnergyStorage(be);
        be.energyHandler = storage;
        return storage;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(long maxAmount, TransactionContext tx) {
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext tx) {
        long taken = Math.min(maxAmount, be.energy());
        if (taken <= 0) return 0;
        updateSnapshots(tx);
        be.setEnergy(be.energy() - taken);
        return taken;
    }

    @Override
    public long getAmount() {
        return be.energy();
    }

    @Override
    public long getCapacity() {
        return be.capacity();
    }

    @Override
    protected Long createSnapshot() {
        return be.energy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        be.setEnergy(snapshot);
    }
}
