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
 * A block's input slots and one output slot, to hoppers and pipes (the tanks' and the Experience
 * Pylon's fill-and-empty slots, the XP Powered Anvil's, the XP Powered Disenchanter's): inserting
 * fills the first input that takes the item; the one view, for extracting, is the output.
 */
@SuppressWarnings("UnstableApiUsage")
public final class InOutSlotsItemStorage extends SnapshotParticipant<ItemStack[]> implements Storage<ItemVariant> {

    /** An input slot: which one, the most it holds, and what it takes. */
    public record Input(int slot, int limit, Predicate<ItemStack> accepts) {}

    private final Container items;
    private final List<Input> inputs;
    private final int output;

    /** One input slot, holding up to {@code inputLimit} of what {@code accepts} allows. */
    public InOutSlotsItemStorage(Container items, int input, int output, int inputLimit, Predicate<ItemStack> accepts) {
        this(items, List.of(new Input(input, inputLimit, accepts)), output);
    }

    public InOutSlotsItemStorage(Container items, List<Input> inputs, int output) {
        this.items = items;
        this.inputs = List.copyOf(inputs);
        this.output = output;
    }

    @Override
    protected ItemStack[] createSnapshot() {
        ItemStack[] copy = new ItemStack[inputs.size() + 1];
        for (int i = 0; i < inputs.size(); i++) copy[i] = items.getItem(inputs.get(i).slot()).copy();
        copy[inputs.size()] = items.getItem(output).copy();
        return copy;
    }

    @Override
    protected void readSnapshot(ItemStack[] snapshot) {
        for (int i = 0; i < inputs.size(); i++) items.setItem(inputs.get(i).slot(), snapshot[i]);
        items.setItem(output, snapshot[inputs.size()]);
    }

    @Override
    protected void onFinalCommit() {
        items.setChanged();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        ItemStack sample = resource.toStack();
        for (Input in : inputs) {
            if (!in.accepts().test(sample)) continue;
            ItemStack held = items.getItem(in.slot());
            if (!held.isEmpty() && !resource.matches(held)) continue;
            int limit = Math.min(in.limit(), sample.getMaxStackSize());
            int toInsert = (int) Math.min(maxAmount, limit - held.getCount());
            if (toInsert <= 0) continue;
            updateSnapshots(transaction);
            if (held.isEmpty()) items.setItem(in.slot(), resource.toStack(toInsert));
            else held.grow(toInsert);
            return toInsert;
        }
        return 0;
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
