package net.bobofraggins.mobfarmingsupplies.incubator.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

/**
 * The XP Powered Incubator to pipes and hoppers. Inserting puts eggs in spread evenly over the
 * input rows, and a DNA sample in its slot; the views, for extracting, are the rows' finished eggs
 * only.
 */
@SuppressWarnings("UnstableApiUsage")
public final class IncubatorItemStorage extends SnapshotParticipant<ItemStack[]> implements Storage<ItemVariant> {

    private final IncubatorBlockEntity be;

    public IncubatorItemStorage(IncubatorBlockEntity be) {
        this.be = be;
    }

    @Override
    protected ItemStack[] createSnapshot() {
        ItemStack[] copy = new ItemStack[IncubatorBlockEntity.SLOTS];
        for (int i = 0; i < copy.length; i++) copy[i] = be.items().getItem(i).copy();
        return copy;
    }

    @Override
    protected void readSnapshot(ItemStack[] snapshot) {
        for (int i = 0; i < snapshot.length; i++) be.items().setItem(i, snapshot[i]);
    }

    @Override
    protected void onFinalCommit() {
        be.setChanged();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        ItemStack incoming = resource.toStack((int) Math.min(maxAmount, resource.toStack().getMaxStackSize()));
        if (be.insert(incoming.copy(), true) == 0) return 0;
        updateSnapshots(transaction);
        return be.insert(incoming, false);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long taken = 0;
        for (int row = 0; row < IncubatorBlockEntity.ROWS && taken < maxAmount; row++) {
            taken += new OutputView(row).extract(resource, maxAmount - taken, transaction);
        }
        return taken;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(IncubatorBlockEntity.ROWS);
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) views.add(new OutputView(row));
        return views.iterator();
    }

    /** A row's finished eggs. */
    private final class OutputView implements StorageView<ItemVariant> {
        private final int slot;

        OutputView(int row) {
            this.slot = IncubatorBlockEntity.FIRST_OUTPUT + row;
        }

        private ItemStack stack() {
            return be.items().getItem(slot);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack held = stack();
            if (held.isEmpty() || maxAmount <= 0 || !resource.matches(held)) return 0;
            int taken = (int) Math.min(maxAmount, held.getCount());
            updateSnapshots(transaction);
            be.items().removeItem(slot, taken);
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
