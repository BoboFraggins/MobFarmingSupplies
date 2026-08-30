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
 *
 * <p>{@link TankBlockEntity#amount} is tracked in Architectury's mB convention (1000 mB =
 * 1 bucket), but the Fabric Transfer API measures fluids in <b>droplets</b>
 * (1 mB = {@value #DROPLETS_PER_MB} droplets — see {@code FluidConstants.BUCKET} = 81000).
 * All {@link Storage} methods convert at this boundary so generic Fabric-side consumers
 * (vanilla bucket fallback, other mods' pipes/tanks) see correctly-scaled amounts.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricTankFluidStorage
        extends SnapshotParticipant<FabricTankFluidStorage.Snapshot>
        implements Storage<FluidVariant> {

    public static final long DROPLETS_PER_MB = 81L;

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
        long spaceDroplets = (TankBlockEntity.CAPACITY - be.amount) * DROPLETS_PER_MB;
        long toInsertMb = Math.min(maxAmount, spaceDroplets) / DROPLETS_PER_MB;
        if (toInsertMb <= 0) return 0;

        updateSnapshots(transaction);
        if (be.storedFluid.isEmpty()) be.storedFluid = incoming.copyWithAmount(1);
        be.amount += toInsertMb;
        return toInsertMb * DROPLETS_PER_MB;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0 || be.storedFluid.isEmpty()) return 0;
        FluidStack req = fromFabric(resource, 1);
        if (!be.storedFluid.isFluidEqual(req) || !be.storedFluid.isComponentEqual(req)) return 0;
        long toExtractMb = Math.min(maxAmount / DROPLETS_PER_MB, be.amount);
        if (toExtractMb <= 0) return 0;

        updateSnapshots(transaction);
        be.amount -= toExtractMb;
        if (be.amount <= 0) {
            be.amount = 0;
            be.storedFluid = FluidStack.empty();
        }
        return toExtractMb * DROPLETS_PER_MB;
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
        public long getAmount() { return be.amount * DROPLETS_PER_MB; }

        @Override
        public long getCapacity() { return TankBlockEntity.CAPACITY * DROPLETS_PER_MB; }
    }
}
