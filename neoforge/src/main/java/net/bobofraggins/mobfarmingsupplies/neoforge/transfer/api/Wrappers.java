package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy.EnergyHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.SnapshotJournal;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Other mods' 1.21.1 handlers as {@link ResourceHandler}s and {@link EnergyHandler}s. Those can't
 * be rolled back, so inside a transaction each move is only simulated, and remembered; the moves
 * are made for real, in order, once the outermost transaction commits. Later simulations in the
 * same transaction allow for the moves still waiting, by simulating the waiting amount and the new
 * one together.
 *
 * <p>Our own handlers, offered to other mods through {@link Adapters}, are unwrapped instead, so
 * our blocks deal with each other inside one real transaction.
 */
public final class Wrappers {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Wrappers() {}

    @Nullable
    @SuppressWarnings("unchecked")
    public static ResourceHandler<ItemResource> items(@Nullable IItemHandler handler) {
        if (handler == null) return null;
        if (handler instanceof Adapters.ItemAdapter adapter) return adapter.handler();
        return new ItemWrapper(handler);
    }

    @Nullable
    public static ResourceHandler<FluidResource> fluids(@Nullable IFluidHandler handler) {
        if (handler == null) return null;
        if (handler instanceof Adapters.FluidAdapter adapter) return adapter.handler();
        return new FluidWrapper(handler);
    }

    @Nullable
    public static EnergyHandler energy(@Nullable IEnergyStorage storage) {
        if (storage == null) return null;
        if (storage instanceof Adapters.EnergyAdapter adapter) return adapter.handler();
        return new EnergyWrapper(storage);
    }

    /** Moves waiting for the outermost transaction to commit. */
    abstract static class Pending<O> extends SnapshotJournal<List<O>> {
        protected List<O> ops = new ArrayList<>();

        void add(O op, TransactionContext tx) {
            updateSnapshots(tx);
            ops.add(op);
        }

        @Override
        protected List<O> createSnapshot() {
            return new ArrayList<>(ops);
        }

        @Override
        protected void revertToSnapshot(List<O> snapshot) {
            ops = snapshot;
        }

        @Override
        protected void onRootCommit(List<O> originalState) {
            List<O> toRun = ops;
            ops = new ArrayList<>();
            for (O op : toRun) run(op);
        }

        protected abstract void run(O op);
    }

    // ── Items ──────────────────────────────────────────────────────────────────────

    private record ItemOp(int slot, ItemResource resource, int amount, boolean insert) {}

    private static final class ItemWrapper extends Pending<ItemOp> implements ResourceHandler<ItemResource> {
        private final IItemHandler handler;

        ItemWrapper(IItemHandler handler) {
            this.handler = handler;
        }

        @Override public int size() { return handler.getSlots(); }

        @Override public ItemResource getResource(int index) { return ItemResource.of(handler.getStackInSlot(index)); }

        @Override public long getAmountAsLong(int index) { return handler.getStackInSlot(index).getCount(); }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            int limit = handler.getSlotLimit(index);
            return resource.isEmpty() ? limit : Math.min(limit, resource.toStack(1).getMaxStackSize());
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && handler.isItemValid(index, resource.toStack(1));
        }

        private int waiting(int slot, ItemResource resource, boolean insert) {
            int total = 0;
            for (ItemOp op : ops) {
                if (op.slot() == slot && op.insert() == insert && op.resource().equals(resource)) total += op.amount();
            }
            return total;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            int waiting = waiting(index, resource, true);
            ItemStack left = handler.insertItem(index, resource.toStack(waiting + amount), true);
            int accepted = Math.max(0, Math.min(amount, waiting + amount - left.getCount() - waiting));
            if (accepted > 0) add(new ItemOp(index, resource, accepted, true), tx);
            return accepted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getStackInSlot(index))) return 0;
            int waiting = waiting(index, resource, false);
            ItemStack got = handler.extractItem(index, waiting + amount, true);
            if (!got.isEmpty() && !resource.matches(got)) return 0;
            int taken = Math.max(0, Math.min(amount, got.getCount() - waiting));
            if (taken > 0) add(new ItemOp(index, resource, taken, false), tx);
            return taken;
        }

        @Override
        protected void run(ItemOp op) {
            if (op.insert()) {
                ItemStack left = handler.insertItem(op.slot(), op.resource().toStack(op.amount()), false);
                if (!left.isEmpty()) LOGGER.warn("{} took less than it said it would: {} lost", handler, left);
            } else {
                ItemStack got = handler.extractItem(op.slot(), op.amount(), false);
                if (got.getCount() != op.amount()) LOGGER.warn("{} gave {} instead of {}", handler, got, op.amount());
            }
        }
    }

    // ── Fluids ─────────────────────────────────────────────────────────────────────

    private record FluidOp(FluidResource resource, int amount, boolean fill) {}

    /**
     * {@code IFluidHandler} fills and drains the handler as a whole, not a tank: a fill goes
     * through tank 0, and a drain through the first tank holding that fluid.
     */
    private static final class FluidWrapper extends Pending<FluidOp> implements ResourceHandler<FluidResource> {
        private final IFluidHandler handler;

        FluidWrapper(IFluidHandler handler) {
            this.handler = handler;
        }

        @Override public int size() { return handler.getTanks(); }

        @Override public FluidResource getResource(int index) { return FluidResource.of(handler.getFluidInTank(index)); }

        @Override public long getAmountAsLong(int index) { return handler.getFluidInTank(index).getAmount(); }

        @Override public long getCapacityAsLong(int index, FluidResource resource) { return handler.getTankCapacity(index); }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return !resource.isEmpty() && handler.isFluidValid(index, resource.toStack(1));
        }

        private int waiting(FluidResource resource, boolean fill) {
            int total = 0;
            for (FluidOp op : ops) {
                if (op.fill() == fill && op.resource().equals(resource)) total += op.amount();
            }
            return total;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
            if (index != 0 || resource.isEmpty() || amount <= 0) return 0;
            int waiting = waiting(resource, true);
            int filled = handler.fill(resource.toStack(waiting + amount), IFluidHandler.FluidAction.SIMULATE);
            int accepted = Math.max(0, Math.min(amount, filled - waiting));
            if (accepted > 0) add(new FluidOp(resource, accepted, true), tx);
            return accepted;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0 || index != firstTankWith(resource)) return 0;
            int waiting = waiting(resource, false);
            FluidStack drained = handler.drain(resource.toStack(waiting + amount), IFluidHandler.FluidAction.SIMULATE);
            int taken = Math.max(0, Math.min(amount, drained.getAmount() - waiting));
            if (taken > 0) add(new FluidOp(resource, taken, false), tx);
            return taken;
        }

        private int firstTankWith(FluidResource resource) {
            for (int i = 0, tanks = handler.getTanks(); i < tanks; i++) {
                if (resource.matches(handler.getFluidInTank(i))) return i;
            }
            return -1;
        }

        @Override
        protected void run(FluidOp op) {
            if (op.fill()) {
                int filled = handler.fill(op.resource().toStack(op.amount()), IFluidHandler.FluidAction.EXECUTE);
                if (filled != op.amount()) LOGGER.warn("{} took {} mB instead of {}", handler, filled, op.amount());
            } else {
                FluidStack drained = handler.drain(op.resource().toStack(op.amount()), IFluidHandler.FluidAction.EXECUTE);
                if (drained.getAmount() != op.amount()) {
                    LOGGER.warn("{} gave {} mB instead of {}", handler, drained.getAmount(), op.amount());
                }
            }
        }
    }

    // ── Energy ─────────────────────────────────────────────────────────────────────

    private record EnergyOp(int amount, boolean receive) {}

    private static final class EnergyWrapper extends Pending<EnergyOp> implements EnergyHandler {
        private final IEnergyStorage storage;

        EnergyWrapper(IEnergyStorage storage) {
            this.storage = storage;
        }

        @Override public long getAmountAsLong() { return storage.getEnergyStored(); }

        @Override public long getCapacityAsLong() { return storage.getMaxEnergyStored(); }

        private int waiting(boolean receive) {
            int total = 0;
            for (EnergyOp op : ops) if (op.receive() == receive) total += op.amount();
            return total;
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            if (amount <= 0 || !storage.canReceive()) return 0;
            int waiting = waiting(true);
            int accepted = Math.max(0, Math.min(amount, storage.receiveEnergy(waiting + amount, true) - waiting));
            if (accepted > 0) add(new EnergyOp(accepted, true), tx);
            return accepted;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            if (amount <= 0 || !storage.canExtract()) return 0;
            int waiting = waiting(false);
            int taken = Math.max(0, Math.min(amount, storage.extractEnergy(waiting + amount, true) - waiting));
            if (taken > 0) add(new EnergyOp(taken, false), tx);
            return taken;
        }

        @Override
        protected void run(EnergyOp op) {
            int moved = op.receive()
                    ? storage.receiveEnergy(op.amount(), false)
                    : storage.extractEnergy(op.amount(), false);
            if (moved != op.amount()) LOGGER.warn("{} moved {} FE instead of {}", storage, moved, op.amount());
        }
    }
}
