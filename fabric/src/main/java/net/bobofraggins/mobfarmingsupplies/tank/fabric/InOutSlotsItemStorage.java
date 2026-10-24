package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * A block's one input slot and one output slot, to hoppers and pipes (the tanks' and the
 * Experience Pylon's fill-and-empty slots, the XP Powered Anvil's): inserting fills the input,
 * with only what {@code accepts} allows; the one view, for extracting, is the output.
 */
@SuppressWarnings("UnstableApiUsage")
public final class InOutSlotsItemStorage extends SnapshotParticipant<ItemStack[]> implements Storage<ItemVariant> {

    private final Container items;
    private final int input;
    private final int output;
    private final int inputLimit;
    private final Predicate<ItemStack> accepts;

    /** @param inputLimit the most the input slot holds (1 for the anvil, a stack for a tank) */
    public InOutSlotsItemStorage(Container items, int input, int output, int inputLimit, Predicate<ItemStack> accepts) {
        this.items = items;
        this.input = input;
        this.output = output;
        this.inputLimit = inputLimit;
        this.accepts = accepts;
    }

    @Override
    protected ItemStack[] createSnapshot() {
        return new ItemStack[] {items.getItem(input).copy(), items.getItem(output).copy()};
    }

    @Override
    protected void readSnapshot(ItemStack[] snapshot) {
        items.setItem(input, snapshot[0]);
        items.setItem(output, snapshot[1]);
    }

    @Override
    protected void onFinalCommit() {
        items.setChanged();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0 || !accepts.test(resource.toStack())) return 0;
        ItemStack held = items.getItem(input);
        if (!held.isEmpty() && !resource.matches(held)) return 0;
        int limit = Math.min(inputLimit, resource.toStack().getMaxStackSize());
        int toInsert = (int) Math.min(maxAmount, limit - held.getCount());
        if (toInsert <= 0) return 0;
        updateSnapshots(transaction);
        if (held.isEmpty()) items.setItem(input, resource.toStack(toInsert));
        else held.grow(toInsert);
        return toInsert;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return new OutputView().extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return List.<StorageView<ItemVariant>>of(new OutputView()).iterator();
    }

    private final class OutputView implements StorageView<ItemVariant> {
        private ItemStack stack() {
            return items.getItem(output);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack held = stack();
            if (held.isEmpty() || maxAmount <= 0 || !resource.matches(held)) return 0;
            int taken = (int) Math.min(maxAmount, held.getCount());
            updateSnapshots(transaction);
            items.removeItem(output, taken);
            return taken;
        }

        @Override
        public boolean isResourceBlank() {
            return stack().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(stack());
        }

        @Override
        public long getAmount() {
            return stack().getCount();
        }

        @Override
        public long getCapacity() {
            return stack().isEmpty() ? 64 : stack().getMaxStackSize();
        }
    }
}
