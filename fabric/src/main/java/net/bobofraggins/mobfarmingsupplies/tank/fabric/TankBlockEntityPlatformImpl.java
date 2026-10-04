package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.fluid.fabric.FabricFluidUnits;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TankBlockEntityPlatformImpl {

    private TankBlockEntityPlatformImpl() {}

    /** One slot holding the item being emptied or filled; it keeps whatever the transfer turns the item into. */
    private static final class ItemSlot extends SingleVariantStorage<ItemVariant> {
        ItemSlot(ItemStack stack) {
            this.variant = ItemVariant.of(stack);
            this.amount = stack.getCount();
        }

        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return variant.isBlank() ? 1 : variant.toStack().getMaxStackSize();
        }
    }

    @Nullable
    public static ItemStack tryTransferFluidWithItem(TankBlockEntity be, ItemStack input) {
        // A real one-slot holder rather than ContainerItemContext.withConstant: a constant context
        // throws away what the transfer does to the item, so an emptied bucket read back as still
        // full and the fluid was duplicated. The result below is read back from this slot.
        ContainerItemContext ctx = ContainerItemContext.ofSingleSlot(new ItemSlot(input));
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(input, ctx);
        if (storage == null) return null;

        // ── Try draining fluid FROM the item INTO the tank ──────────────────────
        try (Transaction peekTx = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage) {
                if (view.isResourceBlank() || view.getAmount() <= 0) continue;

                FluidVariant variant = view.getResource();
                FluidStack incoming = FabricTankFluidStorage.fromFabric(variant, 1);

                // Check tank fluid compatibility
                if (!be.storedFluid.isEmpty()
                        && (!be.storedFluid.isFluidEqual(incoming)
                                || !be.storedFluid.isComponentEqual(incoming))) {
                    continue;
                }

                // The item's storage counts in droplets, the tank in mB.
                long space = be.getCapacity() - be.amount;
                long toDrain = Math.min(FabricFluidUnits.toMb(view.getAmount()), space);
                if (toDrain <= 0) continue;

                long extracted = FabricFluidUnits.extractMb(storage, variant, toDrain, peekTx);
                if (extracted > 0) {
                    be.insert(FluidStack.create(incoming, extracted), extracted, false);
                    peekTx.commit();

                    ItemVariant resultVariant = ctx.getMainSlot().getResource();
                    long resultCount = ctx.getMainSlot().getAmount();
                    return resultVariant.isBlank() ? ItemStack.EMPTY
                            : resultVariant.toStack((int) resultCount);
                }
                break;
            }
        }

        // ── Try filling the item FROM the tank ──────────────────────────────────
        if (be.storedFluid.isEmpty() || be.amount == 0) return null;

        FluidVariant toInsert = FabricTankFluidStorage.toFabric(be.storedFluid);
        try (Transaction fillTx = Transaction.openOuter()) {
            long inserted = FabricFluidUnits.insertMb(storage, toInsert, be.amount, fillTx);
            if (inserted > 0) {
                be.extract(inserted, false);
                fillTx.commit();

                ItemVariant resultVariant = ctx.getMainSlot().getResource();
                long resultCount = ctx.getMainSlot().getAmount();
                return resultVariant.isBlank() ? ItemStack.EMPTY
                        : resultVariant.toStack((int) resultCount);
            }
        }

        return null;
    }

    public static void onCapabilitiesChanged(Level level, BlockPos pos) {
        // No-op on Fabric — capabilities are registered differently
    }

    public static boolean isFluidContainer(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)) != null;
    }
}
