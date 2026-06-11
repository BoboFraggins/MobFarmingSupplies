package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes the Absorption Hopper's 16-slot inventory as a {@link ResourceHandler}{@code <ItemResource>}
 * for automation (pipes, other mods).
 */
public class AbsorptionHopperItemHandler implements ResourceHandler<ItemResource> {

    private final AbsorptionHopperBlockEntity be;

    private record Snapshot(ItemStack[] items) {}

    private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
        @Override
        protected Snapshot createSnapshot() {
            ItemStack[] copy = new ItemStack[AbsorptionHopperBlockEntity.SLOT_COUNT];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = be.inventory.getItem(i).copy();
            }
            return new Snapshot(copy);
        }

        @Override
        protected void revertToSnapshot(Snapshot snap) {
            for (int i = 0; i < snap.items().length; i++) {
                be.inventory.setItem(i, snap.items()[i]);
            }
        }

        @Override
        protected void onRootCommit(Snapshot originalState) {
            be.setChanged();
        }
    };

    public AbsorptionHopperItemHandler(AbsorptionHopperBlockEntity be) {
        this.be = be;
    }

    @Override
    public int size() {
        return AbsorptionHopperBlockEntity.SLOT_COUNT;
    }

    @Override
    public ItemResource getResource(int index) {
        ItemStack stack = be.inventory.getItem(index);
        return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return be.inventory.getItem(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? 0 : resource.toStack(1).getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !resource.isEmpty();
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        ItemStack existing = be.inventory.getItem(index);
        ItemStack incoming = resource.toStack(amount);

        if (existing.isEmpty()) {
            int toInsert = Math.min(amount, incoming.getMaxStackSize());
            if (tx != null) journal.updateSnapshots(tx);
            be.inventory.setItem(index, resource.toStack(toInsert));
            if (tx == null) be.setChanged();
            return toInsert;
        }
        if (!ItemStack.isSameItemSameComponents(existing, incoming)) return 0;
        int space = existing.getMaxStackSize() - existing.getCount();
        int toInsert = Math.min(amount, space);
        if (toInsert <= 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        existing.grow(toInsert);
        if (tx == null) be.setChanged();
        return toInsert;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (resource.isEmpty() || amount <= 0) return 0;
        ItemStack existing = be.inventory.getItem(index);
        if (existing.isEmpty() || !resource.matches(existing)) return 0;
        int toExtract = Math.min(amount, existing.getCount());
        if (tx != null) journal.updateSnapshots(tx);
        be.inventory.removeItem(index, toExtract);
        if (tx == null) be.setChanged();
        return toExtract;
    }
}
