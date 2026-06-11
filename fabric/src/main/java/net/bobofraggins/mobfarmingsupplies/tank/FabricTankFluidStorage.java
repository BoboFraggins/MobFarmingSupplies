package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

/**
 * Fabric {@link Storage}{@code <FluidVariant>} backed by a {@link TankBlockEntity}.
 *
 * <p>Registered via {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage#SIDED}
 * fallback in {@link net.bobofraggins.mobfarmingsupplies.fabric.MobFarmingSuppliesFabric}.
 * Uses {@link SnapshotParticipant} for full transaction rollback support.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricTankFluidStorage
        extends SnapshotParticipant<FabricTankFluidStorage.Snapshot>
        implements Storage<FluidVariant> {

    private final TankBlockEntity be;

    public FabricTankFluidStorage(TankBlockEntity be) {
        this.be = be;
    }

    // ── SnapshotParticipant ─────────────────────────────────────────────────────

    record Snapshot(FluidStack fluid, long amount) {}

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(be.storedFluid.copy(), be.amount);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        be.storedFluid = snapshot.fluid();
        be.amount = snapshot.amount();
    }

    @Override
    protected void onFinalCommit() {
        be.notifyFluidChanged();
    }

    // ── Storage<FluidVariant> ───────────────────────────────────────────────────

    @Override
    public boolean supportsInsertion() { return true; }

    @Override
    public boolean supportsExtraction() { return true; }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        FluidStack incoming = fromFabric(resource, 1);
        if (!be.storedFluid.isEmpty()
                && (!be.storedFluid.isFluidEqual(incoming) || !be.storedFluid.isComponentEqual(incoming))) {
            return 0;
        }
        long space = TankBlockEntity.CAPACITY - be.amount;
        long toInsert = Math.min(maxAmount, space);
        if (toInsert <= 0) return 0;

        updateSnapshots(transaction);
        if (be.storedFluid.isEmpty()) be.storedFluid = incoming.copyWithAmount(1);
        be.amount += toInsert;
        return toInsert;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0 || be.storedFluid.isEmpty()) return 0;
        FluidStack req = fromFabric(resource, 1);
        if (!be.storedFluid.isFluidEqual(req) || !be.storedFluid.isComponentEqual(req)) return 0;
        long toExtract = Math.min(maxAmount, be.amount);
        if (toExtract <= 0) return 0;

        updateSnapshots(transaction);
        be.amount -= toExtract;
        if (be.amount <= 0) {
            be.amount = 0;
            be.storedFluid = FluidStack.empty();
        }
        return toExtract;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.singletonList((StorageView<FluidVariant>) new TankView()).iterator();
    }

    // ── Conversion helpers (package-accessible for TankBlockEntityPlatformImpl) ─

    public static FluidStack fromFabric(FluidVariant variant, long amount) {
        return FluidStack.create(variant.getFluid(), amount, variant.getComponentsPatch());
    }

    public static FluidVariant toFabric(FluidStack archFluid) {
        return FluidVariant.of(archFluid.getFluid(), archFluid.getPatch());
    }

    // ── Inner view ──────────────────────────────────────────────────────────────

    private final class TankView implements StorageView<FluidVariant> {

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
            return FabricTankFluidStorage.this.extract(resource, maxAmount, tx);
        }

        @Override
        public boolean isResourceBlank() { return be.storedFluid.isEmpty(); }

        @Override
        public FluidVariant getResource() {
            return be.storedFluid.isEmpty() ? FluidVariant.blank() : toFabric(be.storedFluid);
        }

        @Override
        public long getAmount() { return be.amount; }

        @Override
        public long getCapacity() { return TankBlockEntity.CAPACITY; }
    }
}
