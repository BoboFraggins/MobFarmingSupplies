package net.bobofraggins.mobfarmingsupplies.absorptionhopper.fabric;

import net.bobofraggins.mobfarmingsupplies.transfer.fabric.HopperLookups;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.IAbsorptionHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

@SuppressWarnings("UnstableApiUsage")
public final class AbsorptionHopperBlockEntityPlatformImpl {

    /**
     * {@link AbsorptionHopperBlockEntity#tankAmount} is tracked in Architectury's mB
     * convention, but the Fabric Transfer API measures fluids in droplets (1 mB =
     * {@value} droplets — see {@code FluidConstants.BUCKET} = 81000). {@code dest} in
     * {@link #pushFluid} is an arbitrary adjacent block's storage, so it must be treated
     * as droplet-based.
     */
    private static final long DROPLETS_PER_MB = 81L;

    private AbsorptionHopperBlockEntityPlatformImpl() {}

    public static boolean pushItems(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        boolean moved = false;
        int sides = be.getPushSides();
        for (int bit = 0; bit < 6; bit++) {
            if ((sides & (1 << bit)) == 0) continue;
            Direction dir = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            Storage<ItemVariant> itemStorage = HopperLookups.of(be).at(level, pos, dir, ItemStorage.SIDED);
            if (itemStorage != null && !HopperLookups.definitelyFull(itemStorage)) moved |= pushItems(be, itemStorage);
        }
        return moved;
    }

    public static boolean pushFluid(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        boolean moved = false;
        int sides = be.getPushSides();
        for (int bit = 0; bit < 6; bit++) {
            if ((sides & (1 << bit)) == 0) continue;
            if (be.tankAmount <= 0 || be.tankFluid.isEmpty()) break;
            Direction dir = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            Storage<FluidVariant> fluidStorage = HopperLookups.of(be).at(level, pos, dir, FluidStorage.SIDED);
            if (fluidStorage != null && !HopperLookups.definitelyFull(fluidStorage)) moved |= pushFluid(be, fluidStorage);
        }
        return moved;
    }

    /** Pushes up to the hoppers' items-per-transfer into {@code dest}, from as many slots as it takes. */
    private static boolean pushItems(AbsorptionHopperBlockEntity be, Storage<ItemVariant> dest) {
        long budget = MFSConfig.getHopperItemsPerTransfer();
        boolean movedAny = false;
        for (int slot = 0; slot < AbsorptionHopperBlockEntity.SLOT_COUNT && budget > 0; slot++) {
            ItemStack stack = be.inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            ItemVariant variant = ItemVariant.of(stack);
            long toSend = Math.min(stack.getCount(), budget);
            long sent;
            try (Transaction tx = Transaction.openOuter()) {
                sent = dest.insert(variant, toSend, tx);
                if (sent > 0) tx.commit();
                else sent = 0;
            }
            if (sent > 0) {
                be.inventory.removeItem(slot, (int) sent);
                be.setChanged();
                budget -= sent;
                movedAny = true;
            }
        }
        return movedAny;
    }

    private static boolean pushFluid(AbsorptionHopperBlockEntity be, Storage<FluidVariant> dest) {
        long toSendMb = Math.min(TransferSchedule.perTransfer(MFSConfig.getHopperFluidPerTick()), be.tankAmount);
        FluidVariant variant = FluidVariant.of(be.tankFluid.getFluid(), be.tankFluid.getPatch());
        long sentMb;
        try (Transaction tx = Transaction.openOuter()) {
            long sentDroplets = dest.insert(variant, toSendMb * DROPLETS_PER_MB, tx);
            sentMb = sentDroplets / DROPLETS_PER_MB;
            if (sentMb > 0) tx.commit();
            else sentMb = 0;
        }
        if (sentMb > 0) {
            be.tankAmount -= (int) sentMb;
            if (be.tankAmount <= 0) {
                be.tankAmount = 0;
                be.tankFluid = FluidStack.empty();
            }
            be.setChanged();
            return true;
        }
        return false;
    }
}
