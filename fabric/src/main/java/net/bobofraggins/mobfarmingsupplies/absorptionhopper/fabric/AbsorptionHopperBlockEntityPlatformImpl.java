package net.bobofraggins.mobfarmingsupplies.absorptionhopper.fabric;

import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.IAbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.fluid.fabric.FabricFluidUnits;
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

    private AbsorptionHopperBlockEntityPlatformImpl() {}

    public static void outputPhase(AbsorptionHopperBlockEntity be, Level level, BlockPos pos, boolean itemTick) {
        int sides = be.getPushSides();
        for (int bit = 0; bit < 6; bit++) {
            if ((sides & (1 << bit)) == 0) continue;
            Direction dir = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            BlockPos adj = pos.relative(dir);
            Direction fromSide = dir.getOpposite();

            if (itemTick) {
                Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, adj, fromSide);
                if (itemStorage != null) pushItems(be, itemStorage);
            }

            if (be.tankAmount > 0 && !be.tankFluid.isEmpty()) {
                Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(level, adj, fromSide);
                if (fluidStorage != null) pushFluid(be, fluidStorage);
            }
        }
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
        long toSend = Math.min(MFSConfig.getHopperFluidPerTick(), be.tankAmount);
        FluidVariant variant = FluidVariant.of(be.tankFluid.getFluid(), be.tankFluid.getPatch());
        long sent;
        try (Transaction tx = Transaction.openOuter()) {
            sent = FabricFluidUnits.insertMb(dest, variant, toSend, tx); // mB in, mB out
            if (sent > 0) tx.commit();
            else sent = 0;
        }
        if (sent > 0) {
            be.tankAmount -= (int) sent;
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
