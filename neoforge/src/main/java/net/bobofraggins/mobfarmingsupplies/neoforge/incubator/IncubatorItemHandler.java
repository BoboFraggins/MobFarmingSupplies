package net.bobofraggins.mobfarmingsupplies.neoforge.incubator;

import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The XP Powered Incubator to pipes and hoppers. Index 0 takes eggs (spread evenly over the input
 * rows) and DNA samples; indices 1-4 are the rows' finished eggs, to take out. The inputs and DNA
 * slot can't be pulled from.
 */
public class IncubatorItemHandler implements ResourceHandler<ItemResource> {

    private static final int OUTPUTS = IncubatorBlockEntity.ROWS;

    private final IncubatorBlockEntity be;

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] copy = new ItemStack[IncubatorBlockEntity.SLOTS];
            for (int i = 0; i < copy.length; i++) copy[i] = be.items().getItem(i).copy();
            return copy;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < snapshot.length; i++) be.items().setItem(i, snapshot[i]);
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            be.setChanged();
        }
    };

    public IncubatorItemHandler(IncubatorBlockEntity be) {
        this.be = be;
    }

    private ItemStack output(int index) {
        return be.items().getItem(IncubatorBlockEntity.FIRST_OUTPUT + index - 1);
    }

    @Override
    public int size() {
        return 1 + OUTPUTS;
    }

    @Override
    public ItemResource getResource(int index) {
        if (index == 0) return ItemResource.EMPTY;
        ItemStack stack = output(index);
        return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return index == 0 ? 0 : output(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? 64 : resource.toStack(1).getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (index != 0 || resource.isEmpty()) return false;
        ItemStack stack = resource.toStack(1);
        return IncubatorMenu.isInput(stack) || IncubatorMenu.isSample(stack);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index != 0 || resource.isEmpty() || amount <= 0) return 0;
        ItemStack incoming = resource.toStack(amount);
        if (be.insert(incoming.copy(), true) == 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        int inserted = be.insert(incoming, false);
        if (tx == null) be.setChanged();
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index == 0 || resource.isEmpty() || amount <= 0) return 0;
        ItemStack held = output(index);
        if (held.isEmpty() || !resource.matches(held)) return 0;
        int taken = Math.min(amount, held.getCount());
        if (tx != null) journal.updateSnapshots(tx);
        be.items().removeItem(IncubatorBlockEntity.FIRST_OUTPUT + index - 1, taken);
        if (tx == null) be.setChanged();
        return taken;
    }
}
