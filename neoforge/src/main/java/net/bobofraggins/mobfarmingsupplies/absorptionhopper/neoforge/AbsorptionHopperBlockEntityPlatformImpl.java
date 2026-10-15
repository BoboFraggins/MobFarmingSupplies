package net.bobofraggins.mobfarmingsupplies.absorptionhopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.HopperLookups;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandlerUtil;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.IAbsorptionHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Capabilities;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;

public final class AbsorptionHopperBlockEntityPlatformImpl {

    private AbsorptionHopperBlockEntityPlatformImpl() {}

    public static boolean pushItems(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        boolean moved = false;
        int sides = be.getPushSides();
        for (int bit = 0; bit < 6; bit++) {
            if ((sides & (1 << bit)) == 0) continue;
            Direction dir = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            ResourceHandler<ItemResource> itemH = HopperLookups.of(be).at(level, pos, dir, Capabilities.Item.BLOCK);
            if (itemH != null && !HopperLookups.definitelyFull(itemH)) moved |= pushItems(be, itemH);
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
            ResourceHandler<FluidResource> fluidH = HopperLookups.of(be).at(level, pos, dir, Capabilities.Fluid.BLOCK);
            if (fluidH != null && !HopperLookups.definitelyFull(fluidH)) moved |= pushFluid(be, fluidH);
        }
        return moved;
    }

    /** Pushes up to the hoppers' items-per-transfer into {@code dest}, from as many slots as it takes. */
    private static boolean pushItems(AbsorptionHopperBlockEntity be, ResourceHandler<ItemResource> dest) {
        int budget = MFSConfig.getHopperItemsPerTransfer();
        boolean movedAny = false;
        for (int slot = 0; slot < AbsorptionHopperBlockEntity.SLOT_COUNT && budget > 0; slot++) {
            ItemStack stack = be.inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            ItemResource res = ItemResource.of(stack);
            int toSend = Math.min(stack.getCount(), budget);
            int sent;
            try (Transaction tx = Transaction.openRoot()) {
                // Topping up partial stacks first, then empty slots.
                sent = ResourceHandlerUtil.insertStacking(dest, res, toSend, tx);
                if (sent > 0) tx.commit();
            }
            if (sent > 0) {
                be.inventory.removeItem(slot, sent);
                be.setChanged();
                budget -= sent;
                movedAny = true;
            }
        }
        return movedAny;
    }

    private static boolean pushFluid(AbsorptionHopperBlockEntity be, ResourceHandler<FluidResource> dest) {
        int toSend = Math.min(TransferSchedule.perTransfer(MFSConfig.getHopperFluidPerTick()), be.tankAmount);
        FluidResource res = FluidResource.of(FluidStackHooksForge.toForge(be.tankFluid));
        int sent;
        try (Transaction tx = Transaction.openRoot()) {
            sent = ResourceHandlerUtil.insertStacking(dest, res, toSend, tx);
            if (sent > 0) tx.commit();
        }
        if (sent > 0) {
            be.tankAmount -= sent;
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
