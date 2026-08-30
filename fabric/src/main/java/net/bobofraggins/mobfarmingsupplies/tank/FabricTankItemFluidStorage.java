package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Iterator;

/**
 * Fabric {@link Storage}{@code <FluidVariant>} for the Tank block item.
 *
 * <p>Reads and writes fluid state via the {@link TankContents} data component on the
 * item stack, propagating changes back to the inventory through {@link ContainerItemContext}.
 *
 * <p>Registered via {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage#ITEM}
 * fallback in {@link net.bobofraggins.mobfarmingsupplies.fabric.MobFarmingSuppliesFabric}.
 *
 * <p>{@link TankContents#amount()} is tracked in Architectury's mB convention, but the
 * Fabric Transfer API measures fluids in <b>droplets</b> (1 mB = {@value #DROPLETS_PER_MB}
 * droplets — see {@code FluidConstants.BUCKET} = 81000). All {@link Storage} methods
 * convert at this boundary so this storage correctly interoperates with generic
 * Fabric-side consumers.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricTankItemFluidStorage implements Storage<FluidVariant> {

    private static final long DROPLETS_PER_MB = 81L;

    private final ContainerItemContext ctx;

    public FabricTankItemFluidStorage(ItemStack stack, ContainerItemContext ctx) {
        this.ctx = ctx;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private TankContents contents() {
        ItemStack stack = ctx.getItemVariant().toStack((int) ctx.getAmount());
        TankContents c = stack.get(Registration.TANK_CONTENTS.get());
        return c != null ? c : TankContents.EMPTY;
    }

    private boolean updateContents(TankContents updated, TransactionContext tx) {
        ItemStack stack = ctx.getItemVariant().toStack((int) ctx.getAmount());
        stack.set(Registration.TANK_CONTENTS.get(), updated);
        long inserted = ctx.exchange(ItemVariant.of(stack), 1, tx);
        return inserted > 0;
    }

    // ── Storage<FluidVariant> ───────────────────────────────────────────────────

    @Override
    public boolean supportsInsertion() { return true; }

    @Override
    public boolean supportsExtraction() { return true; }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        TankContents c = contents();
        FluidStack incoming = FabricTankFluidStorage.fromFabric(resource, 1);
        if (!c.storedFluid().isEmpty()
                && (!c.storedFluid().isFluidEqual(incoming) || !c.storedFluid().isComponentEqual(incoming))) {
            return 0;
        }
        long spaceDroplets = (TankBlockEntity.CAPACITY - c.amount()) * DROPLETS_PER_MB;
        long toFillMb = Math.min(maxAmount, spaceDroplets) / DROPLETS_PER_MB;
        if (toFillMb <= 0) return 0;

        FluidStack newFluid = c.storedFluid().isEmpty() ? incoming.copyWithAmount(1) : c.storedFluid();
        TankContents updated = new TankContents(newFluid, c.amount() + toFillMb, c.bucketMode());
        return updateContents(updated, transaction) ? toFillMb * DROPLETS_PER_MB : 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        TankContents c = contents();
        if (c.storedFluid().isEmpty()) return 0;
        FluidStack req = FabricTankFluidStorage.fromFabric(resource, 1);
        if (!c.storedFluid().isFluidEqual(req) || !c.storedFluid().isComponentEqual(req)) return 0;
        long toDrainMb = Math.min(maxAmount / DROPLETS_PER_MB, c.amount());
        if (toDrainMb <= 0) return 0;

        TankContents updated = new TankContents(c.storedFluid(), c.amount() - toDrainMb, c.bucketMode());
        return updateContents(updated, transaction) ? toDrainMb * DROPLETS_PER_MB : 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.singletonList((StorageView<FluidVariant>) new ItemView()).iterator();
    }

    // ── Inner view ──────────────────────────────────────────────────────────────

    private final class ItemView implements StorageView<FluidVariant> {

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
            return FabricTankItemFluidStorage.this.extract(resource, maxAmount, tx);
        }

        @Override
        public boolean isResourceBlank() { return contents().storedFluid().isEmpty(); }

        @Override
        public FluidVariant getResource() {
            TankContents c = contents();
            return c.storedFluid().isEmpty() ? FluidVariant.blank()
                    : FabricTankFluidStorage.toFabric(c.storedFluid());
        }

        @Override
        public long getAmount() { return contents().amount() * DROPLETS_PER_MB; }

        @Override
        public long getCapacity() { return TankBlockEntity.CAPACITY * DROPLETS_PER_MB; }
    }
}
