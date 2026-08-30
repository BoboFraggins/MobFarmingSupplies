package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.neoforge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes the Absorption Hopper's XP fluid tank as a {@link ResourceHandler}{@code <FluidResource>}.
 *
 * <p>Only accepts fluids tagged {@code c:experience}.
 */
public class AbsorptionHopperFluidHandler implements ResourceHandler<FluidResource> {

    private final AbsorptionHopperBlockEntity be;

    private record Snapshot(FluidStack tankFluid, int tankAmount) {}

    private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(be.tankFluid.copy(), be.tankAmount);
        }

        @Override
        protected void revertToSnapshot(Snapshot snap) {
            be.tankFluid = snap.tankFluid();
            be.tankAmount = snap.tankAmount();
        }

        @Override
        protected void onRootCommit(Snapshot originalState) {
            be.setChanged();
        }
    };

    public AbsorptionHopperFluidHandler(AbsorptionHopperBlockEntity be) {
        this.be = be;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        return be.tankFluid.isEmpty() ? FluidResource.EMPTY
                : FluidResource.of(FluidStackHooksForge.toForge(be.tankFluid));
    }

    @Override
    public long getAmountAsLong(int index) {
        return be.tankAmount;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return AbsorptionHopperBlockEntity.TANK_CAPACITY;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) return false;
        return resource.toStack(1).is(Registration.TAG_EXPERIENCE_FLUID);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (!isValid(index, resource) || amount <= 0) return 0;
        if (!be.tankFluid.isEmpty() && !resource.matches(FluidStackHooksForge.toForge(be.tankFluid))) return 0;
        int space = AbsorptionHopperBlockEntity.TANK_CAPACITY - be.tankAmount;
        int toInsert = Math.min(amount, space);
        if (toInsert <= 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        if (be.tankFluid.isEmpty()) be.tankFluid = FluidStackHooksForge.fromForge(resource.toStack(1));
        be.tankAmount += toInsert;
        if (tx == null) be.setChanged();
        return toInsert;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        if (be.tankFluid.isEmpty() || !resource.matches(FluidStackHooksForge.toForge(be.tankFluid))) return 0;
        int toExtract = Math.min(amount, be.tankAmount);
        if (toExtract <= 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        be.tankAmount -= toExtract;
        if (be.tankAmount <= 0) {
            be.tankAmount = 0;
            be.tankFluid = FluidStack.empty();
        }
        if (tx == null) be.setChanged();
        return toExtract;
    }
}
