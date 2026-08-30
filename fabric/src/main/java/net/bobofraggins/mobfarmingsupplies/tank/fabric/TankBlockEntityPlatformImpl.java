package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TankBlockEntityPlatformImpl {

    /**
     * {@link TankBlockEntity#amount} is tracked in Architectury's mB convention, but the
     * Fabric Transfer API measures fluids in droplets (1 mB = {@value} droplets — see
     * {@code FluidConstants.BUCKET} = 81000). Every {@code storage.insert}/{@code extract}
     * call below must convert at this boundary, since {@code storage} may be a generic
     * Fabric-side provider (e.g. the vanilla bucket fallback) that only understands
     * droplets.
     */
    private static final long DROPLETS_PER_MB = 81L;

    private TankBlockEntityPlatformImpl() {}

    @Nullable
    public static ItemStack tryTransferFluidWithItem(TankBlockEntity be, ItemStack input) {
        // withConstant() gives a read-only/simulation-only context — its insert() is a
        // hard no-op (always returns 0), so item-side mutations like the Experience
        // Syringe's stored-XP data component (set via ContainerItemContext#exchange,
        // which internally requires a successful insert) can never actually persist.
        // Back the context by the tank's real transfer-container slot instead, so
        // exchanges genuinely write through.
        SingleSlotStorage<ItemVariant> slot = ContainerStorage.of(be.transferContainer, null).getSlot(0);
        ContainerItemContext ctx = ContainerItemContext.ofSingleSlot(slot);
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

                long spaceDroplets = (TankBlockEntity.CAPACITY - be.amount) * DROPLETS_PER_MB;
                long toDrainDroplets = Math.min(view.getAmount(), spaceDroplets);
                if (toDrainDroplets <= 0) continue;

                long extractedDroplets = storage.extract(variant, toDrainDroplets, peekTx);
                long extractedMb = extractedDroplets / DROPLETS_PER_MB;
                if (extractedMb > 0) {
                    be.insert(FluidStack.create(incoming, extractedMb), extractedMb, false);
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
            long insertedDroplets = storage.insert(toInsert, be.amount * DROPLETS_PER_MB, fillTx);
            long inserted = insertedDroplets / DROPLETS_PER_MB;
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
