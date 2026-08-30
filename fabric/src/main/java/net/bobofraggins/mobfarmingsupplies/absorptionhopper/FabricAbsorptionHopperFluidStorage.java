package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

/**
 * Fabric {@link Storage}{@code <FluidVariant>} backed by an {@link AbsorptionHopperBlockEntity}.
 *
 * <p>Only accepts fluids tagged {@code c:experience}. Uses {@link SnapshotParticipant} for
 * full transaction rollback support.
 *
 * <p>{@link AbsorptionHopperBlockEntity#tankAmount} is tracked in Architectury's mB
 * convention (1000 mB = 1 bucket), but the Fabric Transfer API measures fluids in
 * <b>droplets</b> (1 mB = {@value #DROPLETS_PER_MB} droplets — see
 * {@code FluidConstants.BUCKET} = 81000). All {@link Storage} methods convert at this
 * boundary so generic Fabric-side consumers (vanilla bucket fallback, other mods'
 * pipes/tanks) see correctly-scaled amounts.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricAbsorptionHopperFluidStorage
        extends SnapshotParticipant<FabricAbsorptionHopperFluidStorage.Snapshot>
        implements Storage<FluidVariant> {

    private static final long DROPLETS_PER_MB = 81L;

    private final AbsorptionHopperBlockEntity be;

    public FabricAbsorptionHopperFluidStorage(AbsorptionHopperBlockEntity be) {
        this.be = be;
    }

    // ── SnapshotParticipant ─────────────────────────────────────────────────────

    record Snapshot(FluidStack fluid, int amount) {}

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(be.tankFluid.copy(), be.tankAmount);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        be.tankFluid = snapshot.fluid();
        be.tankAmount = snapshot.amount();
    }

    @Override
    protected void onFinalCommit() {
        be.setChanged();
    }

    // ── Storage<FluidVariant> ───────────────────────────────────────────────────

    @Override
    public boolean supportsInsertion() { return true; }

    @Override
    public boolean supportsExtraction() { return true; }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        if (!resource.getFluid().is(Registration.TAG_EXPERIENCE_FLUID)) return 0;

        FluidStack incoming = FluidStack.create(resource.getFluid(), 1, resource.getComponentsPatch());
        if (!be.tankFluid.isEmpty()
                && (!be.tankFluid.isFluidEqual(incoming) || !be.tankFluid.isComponentEqual(incoming))) {
            return 0;
        }
        long spaceDroplets = (long) (AbsorptionHopperBlockEntity.TANK_CAPACITY - be.tankAmount) * DROPLETS_PER_MB;
        long toInsertMb = Math.min(maxAmount, spaceDroplets) / DROPLETS_PER_MB;
        if (toInsertMb <= 0) return 0;

        updateSnapshots(transaction);
        if (be.tankFluid.isEmpty()) be.tankFluid = incoming;
        be.tankAmount += (int) toInsertMb;
        return toInsertMb * DROPLETS_PER_MB;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0 || be.tankFluid.isEmpty()) return 0;
        FluidStack req = FluidStack.create(resource.getFluid(), 1, resource.getComponentsPatch());
        if (!be.tankFluid.isFluidEqual(req) || !be.tankFluid.isComponentEqual(req)) return 0;
        long toExtractMb = Math.min(maxAmount / DROPLETS_PER_MB, be.tankAmount);
        if (toExtractMb <= 0) return 0;

        updateSnapshots(transaction);
        be.tankAmount -= (int) toExtractMb;
        if (be.tankAmount <= 0) {
            be.tankAmount = 0;
            be.tankFluid = FluidStack.empty();
        }
        return toExtractMb * DROPLETS_PER_MB;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.singletonList((StorageView<FluidVariant>) new HopperFluidView()).iterator();
    }

    // ── Conversion helpers ──────────────────────────────────────────────────────

    static FluidStack fromFabric(FluidVariant variant) {
        return FluidStack.create(variant.getFluid(), 1, variant.getComponentsPatch());
    }

    static FluidVariant toFabric(FluidStack fluid) {
        return FluidVariant.of(fluid.getFluid(), fluid.getPatch());
    }

    // ── Inner view ──────────────────────────────────────────────────────────────

    private final class HopperFluidView implements StorageView<FluidVariant> {

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
            return FabricAbsorptionHopperFluidStorage.this.extract(resource, maxAmount, tx);
        }

        @Override
        public boolean isResourceBlank() { return be.tankFluid.isEmpty(); }

        @Override
        public FluidVariant getResource() {
            return be.tankFluid.isEmpty() ? FluidVariant.blank() : toFabric(be.tankFluid);
        }

        @Override
        public long getAmount() { return be.tankAmount * DROPLETS_PER_MB; }

        @Override
        public long getCapacity() { return AbsorptionHopperBlockEntity.TANK_CAPACITY * DROPLETS_PER_MB; }
    }
}
