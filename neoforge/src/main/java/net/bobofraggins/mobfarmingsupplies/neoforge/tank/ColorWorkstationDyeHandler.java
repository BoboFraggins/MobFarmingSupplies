package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The Color Workstation's sixteen dyes to hoppers and pipes: pull-only, each dye taken spending
 * the buffer. The dyes are made from the buffer, so a transaction only has to remember its amount.
 */
public class ColorWorkstationDyeHandler implements ResourceHandler<ItemResource> {

    private final ColorWorkstationBlockEntity be;
    private final Container dyes;

    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override
        protected Long createSnapshot() {
            return be.getAmount();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            be.setAmount(snapshot);
        }
    };

    public ColorWorkstationDyeHandler(ColorWorkstationBlockEntity be) {
        this.be = be;
        this.dyes = be.dyes();
    }

    @Override
    public int size() {
        return dyes.getContainerSize();
    }

    @Override
    public ItemResource getResource(int index) {
        ItemStack stack = dyes.getItem(index);
        return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return dyes.getItem(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return 64;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0 || index < 0 || index >= size()) return 0;
        ItemStack held = dyes.getItem(index);
        if (held.isEmpty() || !resource.matches(held)) return 0;
        int taken = Math.min(amount, held.getCount());
        if (tx != null) journal.updateSnapshots(tx);
        dyes.removeItem(index, taken);
        return taken;
    }
}
