package net.bobofraggins.mobfarmingsupplies.neoforge.energy;

import net.bobofraggins.mobfarmingsupplies.energy.EnergyBuffer;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** An {@link EnergyBuffer} as a NeoForge energy handler. Changes are undone if their transaction is. */
public final class BufferEnergyHandler implements EnergyHandler {

    private final EnergyBuffer buffer;

    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override
        protected Long createSnapshot() {
            return buffer.energy();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            buffer.setEnergy(snapshot);
        }
    };

    private BufferEnergyHandler(EnergyBuffer buffer) {
        this.buffer = buffer;
    }

    /** The handler for {@code buffer}, made once so every caller shares one journal. */
    public static BufferEnergyHandler of(EnergyBuffer buffer) {
        if (buffer.energyHandler() instanceof BufferEnergyHandler existing) return existing;
        BufferEnergyHandler handler = new BufferEnergyHandler(buffer);
        buffer.setEnergyHandler(handler);
        return handler;
    }

    @Override
    public long getAmountAsLong() {
        return buffer.energy();
    }

    @Override
    public long getCapacityAsLong() {
        return buffer.capacity();
    }

    @Override
    public int insert(int amount, TransactionContext tx) {
        int added = (int) Math.min(amount, Math.min(buffer.maxInsert(), buffer.capacity() - buffer.energy()));
        if (added <= 0) return 0;
        journal.updateSnapshots(tx);
        buffer.setEnergy(buffer.energy() + added);
        return added;
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        int taken = (int) Math.min(amount, Math.min(buffer.maxExtract(), buffer.energy()));
        if (taken <= 0) return 0;
        journal.updateSnapshots(tx);
        buffer.setEnergy(buffer.energy() - taken);
        return taken;
    }
}
