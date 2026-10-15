package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy.EnergyHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Our handlers as 1.21.1 capabilities for other mods. Each call runs in a transaction of its own
 * (inside the one already open on this thread, if any), committed unless it's a simulation.
 */
public final class Adapters {

    private Adapters() {}

    @Nullable
    public static IItemHandler items(@Nullable ResourceHandler<ItemResource> handler) {
        return handler == null ? null : new ItemAdapter(handler);
    }

    @Nullable
    public static IFluidHandler fluids(@Nullable ResourceHandler<FluidResource> handler) {
        return handler == null ? null : new FluidAdapter(handler);
    }

    @Nullable
    public static IEnergyStorage energy(@Nullable EnergyHandler handler) {
        return handler == null ? null : new EnergyAdapter(handler);
    }

    private static int clamp(long value) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
    }

    public record ItemAdapter(ResourceHandler<ItemResource> handler) implements IItemHandler {
        @Override public int getSlots() { return handler.size(); }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemResource resource = handler.getResource(slot);
            return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(clamp(handler.getAmountAsLong(slot)));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return stack;
            int inserted;
            try (Transaction tx = Transaction.openAny()) {
                inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
                if (!simulate) tx.commit();
            }
            return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
            int extracted;
            try (Transaction tx = Transaction.openAny()) {
                extracted = handler.extract(slot, resource, amount, tx);
                if (!simulate) tx.commit();
            }
            return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
        }

        @Override
        public int getSlotLimit(int slot) {
            return clamp(handler.getCapacityAsLong(slot, handler.getResource(slot)));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return handler.isValid(slot, ItemResource.of(stack));
        }
    }

    public record FluidAdapter(ResourceHandler<FluidResource> handler) implements IFluidHandler {
        @Override public int getTanks() { return handler.size(); }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidResource resource = handler.getResource(tank);
            return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(clamp(handler.getAmountAsLong(tank)));
        }

        @Override
        public int getTankCapacity(int tank) {
            return clamp(handler.getCapacityAsLong(tank, handler.getResource(tank)));
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return handler.isValid(tank, FluidResource.of(stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            try (Transaction tx = Transaction.openAny()) {
                int filled = handler.insert(FluidResource.of(resource), resource.getAmount(), tx);
                if (action.execute()) tx.commit();
                return filled;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            return drain(FluidResource.of(resource), resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (int i = 0, size = handler.size(); i < size; i++) {
                FluidResource resource = handler.getResource(i);
                if (!resource.isEmpty() && handler.getAmountAsLong(i) > 0) return drain(resource, maxDrain, action);
            }
            return FluidStack.EMPTY;
        }

        private FluidStack drain(FluidResource resource, int amount, FluidAction action) {
            if (amount <= 0) return FluidStack.EMPTY;
            try (Transaction tx = Transaction.openAny()) {
                int drained = handler.extract(resource, amount, tx);
                if (action.execute()) tx.commit();
                return drained <= 0 ? FluidStack.EMPTY : resource.toStack(drained);
            }
        }
    }

    public record EnergyAdapter(EnergyHandler handler) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (toReceive <= 0) return 0;
            try (Transaction tx = Transaction.openAny()) {
                int received = handler.insert(toReceive, tx);
                if (!simulate) tx.commit();
                return received;
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            if (toExtract <= 0) return 0;
            try (Transaction tx = Transaction.openAny()) {
                int extracted = handler.extract(toExtract, tx);
                if (!simulate) tx.commit();
                return extracted;
            }
        }

        @Override public int getEnergyStored() { return clamp(handler.getAmountAsLong()); }

        @Override public int getMaxEnergyStored() { return clamp(handler.getCapacityAsLong()); }

        @Override public boolean canExtract() { return true; }

        @Override public boolean canReceive() { return true; }
    }
}
