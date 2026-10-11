package net.bobofraggins.mobfarmingsupplies.energy.fabric;

import net.bobofraggins.mobfarmingsupplies.energy.EnergyBuffer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/** An {@link EnergyBuffer} as a Team Reborn Energy storage. Changes are undone if their transaction is. */
public final class BufferEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {

    private final EnergyBuffer buffer;

    private BufferEnergyStorage(EnergyBuffer buffer) {
        this.buffer = buffer;
    }

    /** The storage for {@code buffer}, made once so every caller shares one snapshot history. */
    public static BufferEnergyStorage of(EnergyBuffer buffer) {
        if (buffer.energyHandler() instanceof BufferEnergyStorage existing) return existing;
        BufferEnergyStorage storage = new BufferEnergyStorage(buffer);
        buffer.setEnergyHandler(storage);
        return storage;
    }

    @Override
    public boolean supportsInsertion() {
        return buffer.maxInsert() > 0;
    }

    @Override
    public boolean supportsExtraction() {
        return buffer.maxExtract() > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext tx) {
        long added = Math.min(maxAmount, Math.min(buffer.maxInsert(), buffer.capacity() - buffer.energy()));
        if (added <= 0) return 0;
        updateSnapshots(tx);
        buffer.setEnergy(buffer.energy() + added);
        return added;
    }

    @Override
    public long extract(long maxAmount, TransactionContext tx) {
        long taken = Math.min(maxAmount, Math.min(buffer.maxExtract(), buffer.energy()));
        if (taken <= 0) return 0;
        updateSnapshots(tx);
        buffer.setEnergy(buffer.energy() - taken);
        return taken;
    }

    @Override
    public long getAmount() {
        return buffer.energy();
    }

    @Override
    public long getCapacity() {
        return buffer.capacity();
    }

    @Override
    protected Long createSnapshot() {
        return buffer.energy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        buffer.setEnergy(snapshot);
    }
}
