package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes the Tank as a single-slot {@link ResourceHandler}{@code <FluidResource>}
 * for pumps and fluid automation.
 *
 * <p>Properly supports {@link TransactionContext} via a {@link SnapshotJournal} so
 * simulated passes don't permanently mutate the tank.
 *
 * <p>Converts between Architectury's cross-platform {@link FluidStack} (used by
 * {@link TankBlockEntity}) and NeoForge's {@link FluidResource} using
 * {@link FluidStackHooksForge}.
 */
public class TankFluidHandler implements ResourceHandler<FluidResource> {

    private final TankBlockEntity be;

    private record Snapshot(FluidStack storedFluid, long amount) {}

    private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(be.storedFluid.copy(), be.amount);
        }

        @Override
        protected void revertToSnapshot(Snapshot snap) {
            be.storedFluid = snap.storedFluid();
            be.amount = snap.amount();
        }

        @Override
        protected void onRootCommit(Snapshot originalState) {
            be.notifyFluidChanged();
        }
    };

    public TankFluidHandler(TankBlockEntity be) {
        this.be = be;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        if (be.storedFluid.isEmpty()) return FluidResource.EMPTY;
        return FluidResource.of(FluidStackHooksForge.toForge(be.storedFluid.copyWithAmount(1)));
    }

    @Override
    public long getAmountAsLong(int index) {
        return be.amount;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return TankBlockEntity.CAPACITY;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) return false;
        if (be.storedFluid.isEmpty()) return true;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        return be.storedFluid.isFluidEqual(incoming) && be.storedFluid.isComponentEqual(incoming);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        if (!be.storedFluid.isEmpty()
                && (!be.storedFluid.isFluidEqual(incoming) || !be.storedFluid.isComponentEqual(incoming))) return 0;
        long space = TankBlockEntity.CAPACITY - be.amount;
        int toInsert = (int) Math.min(amount, Math.min(space, Integer.MAX_VALUE));
        if (toInsert <= 0) return 0;

        if (tx != null) journal.updateSnapshots(tx);
        if (be.storedFluid.isEmpty()) be.storedFluid = incoming.copyWithAmount(1);
        be.amount += toInsert;
        if (tx == null) be.notifyFluidChanged();
        return toInsert;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        if (be.storedFluid.isEmpty()) return 0;
        FluidStack incoming = FluidStackHooksForge.fromForge(resource.toStack(1));
        if (!be.storedFluid.isFluidEqual(incoming) || !be.storedFluid.isComponentEqual(incoming)) return 0;
        int toDrain = (int) Math.min(amount, Math.min(be.amount, Integer.MAX_VALUE));
        if (toDrain <= 0) return 0;

        if (tx != null) journal.updateSnapshots(tx);
        be.amount -= toDrain;
        if (be.amount <= 0) {
            be.amount = 0;
            be.storedFluid = FluidStack.empty();
        }
        if (tx == null) be.notifyFluidChanged();
        return toDrain;
    }
}
