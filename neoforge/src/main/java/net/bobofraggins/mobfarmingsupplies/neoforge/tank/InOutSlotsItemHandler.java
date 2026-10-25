package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.SnapshotJournal;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;

/**
 * A block's input slots and one output slot, to hoppers and pipes (the tanks' and the Experience
 * Pylon's fill-and-empty slots, the XP Powered Anvil's, the XP Powered Disenchanter's). The first
 * indices are the inputs, each taking only what its {@link Input} allows and none of them pulled
 * from; the last is the output, which can only be pulled from.
 */
public class InOutSlotsItemHandler implements ResourceHandler<ItemResource> {

    /** An input slot: which one, the most it holds, and what it takes. */
    public record Input(int slot, int limit, Predicate<ItemStack> accepts) {}

    private final Container items;
    private final List<Input> inputs;
    private final int output;

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] copy = new ItemStack[inputs.size() + 1];
            for (int i = 0; i < inputs.size(); i++) copy[i] = items.getItem(inputs.get(i).slot()).copy();
            copy[inputs.size()] = items.getItem(output).copy();
            return copy;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < inputs.size(); i++) items.setItem(inputs.get(i).slot(), snapshot[i]);
            items.setItem(output, snapshot[inputs.size()]);
        }

        @Override
        protected void onRootCommit(ItemStack[] originalState) {
            items.setChanged();
        }
    };

    /** One input slot, holding up to {@code inputLimit} of what {@code accepts} allows. */
    public InOutSlotsItemHandler(Container items, int input, int output, int inputLimit, Predicate<ItemStack> accepts) {
        this(items, List.of(new Input(input, inputLimit, accepts)), output);
    }

    public InOutSlotsItemHandler(Container items, List<Input> inputs, int output) {
        this.items = items;
        this.inputs = List.copyOf(inputs);
        this.output = output;
    }

    private boolean isInput(int index) {
        return index < inputs.size();
    }

    private int slot(int index) {
        return isInput(index) ? inputs.get(index).slot() : output;
    }

    @Override
    public int size() {
        return inputs.size() + 1;
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
        int stackSize = resource.isEmpty() ? 64 : resource.toStack(1).getMaxStackSize();
        return isInput(index) ? Math.min(inputs.get(index).limit(), stackSize) : stackSize;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isInput(index) && !resource.isEmpty() && inputs.get(index).accepts().test(resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (!isValid(index, resource) || amount <= 0) return 0;
        Input in = inputs.get(index);
        ItemStack held = items.getItem(in.slot());
        if (!held.isEmpty() && !resource.matches(held)) return 0;
        int limit = Math.min(in.limit(), resource.toStack(1).getMaxStackSize());
        int toInsert = Math.min(amount, limit - held.getCount());
        if (toInsert <= 0) return 0;
        if (tx != null) journal.updateSnapshots(tx);
        if (held.isEmpty()) items.setItem(in.slot(), resource.toStack(toInsert));
        else held.grow(toInsert);
        if (tx == null) items.setChanged();
        return toInsert;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (isInput(index) || resource.isEmpty() || amount <= 0) return 0;
        ItemStack held = items.getItem(output);
        if (held.isEmpty() || !resource.matches(held)) return 0;
        int taken = Math.min(amount, held.getCount());
        if (tx != null) journal.updateSnapshots(tx);
        items.removeItem(output, taken);
        if (tx == null) items.setChanged();
        return taken;
    }
}
