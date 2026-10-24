package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A block's one input slot and one output slot, to hoppers and pipes (the tanks' and the
 * Experience Pylon's fill-and-empty slots, the XP Powered Anvil's): index 0 is the input, which
 * only takes what {@code accepts} allows and can't be pulled from; index 1 is the output, which
 * can only be pulled from.
 */
public class InOutSlotsItemHandler implements ResourceHandler<ItemResource> {

    private final Container items;
    private final int input;
    private final int output;
    private final int inputLimit;
    private final Predicate<ItemStack> accepts;

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            return new ItemStack[] {items.getItem(input).copy(), items.getItem(output).copy()};
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            items.setItem(input, snapshot[0]);
            items.setItem(output, snapshot[1]);
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            items.setChanged();
        }
    };

    /** @param inputLimit the most the input slot holds (1 for the anvil, a stack for a tank) */
    public InOutSlotsItemHandler(Container items, int input, int output, int inputLimit, Predicate<ItemStack> accepts) {
        this.items = items;
        this.input = input;
        this.output = output;
        this.inputLimit = inputLimit;
        this.accepts = accepts;
    }

    private int slot(int index) {
        return index == 0 ? input : output;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public ItemResource getResource(int index) {
        ItemStack stack = items.getItem(slot(index));
        return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
        return items.getItem(slot(index)).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        if (index != 0) return resource.isEmpty() ? 64 : resource.toStack(1).getMaxStackSize();
        return resource.isEmpty() ? inputLimit : Math.min(inputLimit, resource.toStack(1).getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index == 0 && !resource.isEmpty() && accepts.test(resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index != 0 || resource.isEmpty() || amount <= 0 || !accepts.test(resource.toStack(1))) return 0;
        ItemStack held = items.getItem(input);
        int limit = Math.min(inputLimit, resource.toStack(1).getMaxStackSize());
        if (!held.isEmpty() && !resource.matches(held)) return 0;
        int toInsert = Math.min(amount, limit - held.getCount());
        if (toInsert <= 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        if (held.isEmpty()) items.setItem(input, resource.toStack(toInsert));
        else held.grow(toInsert);
        if (tx == null) items.setChanged();
        return toInsert;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (index != 1 || resource.isEmpty() || amount <= 0) return 0;
        ItemStack held = items.getItem(output);
        if (held.isEmpty() || !resource.matches(held)) return 0;
        int taken = Math.min(amount, held.getCount());
        if (tx != null) journal.updateSnapshots(tx);
        items.removeItem(output, taken);
        if (tx == null) items.setChanged();
        return taken;
    }
}
