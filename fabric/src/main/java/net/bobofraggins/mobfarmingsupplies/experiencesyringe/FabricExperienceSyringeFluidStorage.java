package net.bobofraggins.mobfarmingsupplies.experiencesyringe;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Iterator;

/**
 * Fabric {@link Storage}{@code <FluidVariant>} for the Experience Syringe item.
 *
 * <p>Reads and writes the {@link Registration#EXPERIENCE_SYRINGE_STORED_XP} data component,
 * propagating changes back to the inventory through {@link ContainerItemContext}.
 * Only accepts fluids tagged {@code c:experience}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricExperienceSyringeFluidStorage implements Storage<FluidVariant> {

    private final ContainerItemContext ctx;

    public FabricExperienceSyringeFluidStorage(ContainerItemContext ctx) {
        this.ctx = ctx;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private int storedXp() {
        ItemStack stack = ctx.getItemVariant().toStack((int) ctx.getAmount());
        return stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
    }

    private boolean setStoredXp(int newXp, TransactionContext tx) {
        ItemStack stack = ctx.getItemVariant().toStack((int) ctx.getAmount());
        if (newXp <= 0) {
            stack.remove(Registration.EXPERIENCE_SYRINGE_STORED_XP.get());
        } else {
            stack.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), newXp);
        }
        return ctx.exchange(ItemVariant.of(stack), 1, tx) > 0;
    }

    // ── Storage<FluidVariant> ───────────────────────────────────────────────────

    @Override
    public boolean supportsInsertion() { return true; }

    @Override
    public boolean supportsExtraction() { return true; }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        if (!resource.getFluid().is(Registration.TAG_EXPERIENCE_FLUID)) return 0;

        int stored = storedXp();
        int capacityMb = ExperienceSyringeItem.xpToMb(ExperienceSyringeItem.CAPACITY);
        int storedMb = ExperienceSyringeItem.xpToMb(stored);
        long toInsert = Math.min(maxAmount, capacityMb - storedMb);
        if (toInsert <= 0) return 0;

        int newXp = ExperienceSyringeItem.mbToXp(storedMb + (int) toInsert);
        if (!setStoredXp(newXp, transaction)) return 0;
        return toInsert;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        if (!resource.getFluid().is(Registration.TAG_EXPERIENCE_FLUID)) return 0;

        int stored = storedXp();
        if (stored <= 0) return 0;
        int storedMb = ExperienceSyringeItem.xpToMb(stored);
        long toExtract = Math.min(maxAmount, storedMb);
        if (toExtract <= 0) return 0;

        int newXp = ExperienceSyringeItem.mbToXp(storedMb - (int) toExtract);
        if (!setStoredXp(newXp, transaction)) return 0;
        return toExtract;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.singletonList((StorageView<FluidVariant>) new SyringeView()).iterator();
    }

    // ── Inner view ──────────────────────────────────────────────────────────────

    private final class SyringeView implements StorageView<FluidVariant> {

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
            return FabricExperienceSyringeFluidStorage.this.extract(resource, maxAmount, tx);
        }

        @Override
        public boolean isResourceBlank() { return storedXp() <= 0; }

        @Override
        public FluidVariant getResource() {
            return storedXp() <= 0 ? FluidVariant.blank()
                    : FluidVariant.of(Registration.XP_JUICE_SOURCE.get());
        }

        @Override
        public long getAmount() { return ExperienceSyringeItem.xpToMb(storedXp()); }

        @Override
        public long getCapacity() { return ExperienceSyringeItem.xpToMb(ExperienceSyringeItem.CAPACITY); }
    }
}
