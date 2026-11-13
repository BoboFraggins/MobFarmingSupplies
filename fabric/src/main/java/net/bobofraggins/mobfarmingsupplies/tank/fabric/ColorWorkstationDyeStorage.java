package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * The Color Workstation's sixteen dyes to hoppers and pipes: pull-only, each dye taken spending
 * the buffer. The dyes are made from the buffer, so a transaction only has to remember its amount.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ColorWorkstationDyeStorage extends SnapshotParticipant<Long>
        implements ExtractionOnlyStorage<ItemVariant> {

    private final ColorWorkstationBlockEntity be;
    private final Container dyes;

    public ColorWorkstationDyeStorage(ColorWorkstationBlockEntity be) {
        this.be = be;
        this.dyes = be.dyes();
    }

    @Override
    protected Long createSnapshot() {
        return be.getAmount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        be.setAmount(snapshot);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        for (int i = 0; i < dyes.getContainerSize(); i++) {
            long taken = new DyeView(i).extract(resource, maxAmount, transaction);
            if (taken > 0) return taken;
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(dyes.getContainerSize());
        for (int i = 0; i < dyes.getContainerSize(); i++) views.add(new DyeView(i));
        return views.iterator();
    }

    private final class DyeView implements StorageView<ItemVariant> {
        private final int slot;

        DyeView(int slot) {
            this.slot = slot;
        }

        private ItemStack stack() {
            return dyes.getItem(slot);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack held = stack();
            if (held.isEmpty() || maxAmount <= 0 || !resource.matches(held)) return 0;
            int taken = (int) Math.min(maxAmount, held.getCount());
            updateSnapshots(transaction);
            dyes.removeItem(slot, taken);
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
            return 64;
        }
    }
}
