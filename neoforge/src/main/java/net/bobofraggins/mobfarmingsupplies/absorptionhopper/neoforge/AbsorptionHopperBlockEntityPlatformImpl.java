package net.bobofraggins.mobfarmingsupplies.absorptionhopper.neoforge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.IAbsorptionHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class AbsorptionHopperBlockEntityPlatformImpl {

    private AbsorptionHopperBlockEntityPlatformImpl() {}

    public static void outputPhase(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        int sides = be.getPushSides();
        for (int bit = 0; bit < 6; bit++) {
            if ((sides & (1 << bit)) == 0) continue;
            Direction dir = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            BlockPos adj = pos.relative(dir);
            Direction fromSide = dir.getOpposite();

            ResourceHandler<ItemResource> itemH =
                    level.getCapability(Capabilities.Item.BLOCK, adj, fromSide);
            if (itemH != null) pushItems(be, itemH);

            if (be.tankAmount > 0 && !be.tankFluid.isEmpty()) {
                ResourceHandler<FluidResource> fluidH =
                        level.getCapability(Capabilities.Fluid.BLOCK, adj, fromSide);
                if (fluidH != null) pushFluid(be, fluidH);
            }
        }
    }

    private static boolean pushItems(AbsorptionHopperBlockEntity be, ResourceHandler<ItemResource> dest) {
        for (int slot = 0; slot < AbsorptionHopperBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = be.inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            ItemResource res = ItemResource.of(stack);
            int toSend = stack.getCount();
            int sent = 0;
            try (Transaction tx = Transaction.openRoot()) {
                for (int d = 0; d < dest.size() && sent < toSend; d++) {
                    sent += dest.insert(d, res, toSend - sent, tx);
                }
                if (sent > 0) tx.commit();
            }
            if (sent > 0) {
                be.inventory.removeItem(slot, sent);
                be.setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean pushFluid(AbsorptionHopperBlockEntity be, ResourceHandler<FluidResource> dest) {
        int toSend = Math.min(AbsorptionHopperBlockEntity.PUSH_FLUID_MB, be.tankAmount);
        FluidResource res = FluidResource.of(FluidStackHooksForge.toForge(be.tankFluid));
        int sent = 0;
        try (Transaction tx = Transaction.openRoot()) {
            for (int d = 0; d < dest.size() && sent < toSend; d++) {
                sent += dest.insert(d, res, toSend - sent, tx);
            }
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
