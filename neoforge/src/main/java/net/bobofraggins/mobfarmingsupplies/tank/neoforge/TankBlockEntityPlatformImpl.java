package net.bobofraggins.mobfarmingsupplies.tank.neoforge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.neoforge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public final class TankBlockEntityPlatformImpl {

    private TankBlockEntityPlatformImpl() {}

    @Nullable
    public static ItemStack tryTransferFluidWithItem(TankBlockEntity be, ItemStack input) {
        SimpleContainer tmp = new SimpleContainer(input.copy());
        var wrapper    = VanillaContainerWrapper.of(tmp);
        var itemAccess = ItemAccess.forHandlerIndex(wrapper, 0).oneByOne();
        ResourceHandler<FluidResource> cap =
                tmp.getItem(0).getCapability(Capabilities.Fluid.ITEM, itemAccess);
        if (cap == null) return null;

        FluidResource containedRes = cap.getResource(0);
        long containedAmt = cap.getAmountAsLong(0);

        if (!containedRes.isEmpty() && containedAmt > 0) {
            // Item has fluid → drain into tank
            FluidStack archFluid = FluidStackHooksForge.fromForge(containedRes.toStack(1));
            if (!be.storedFluid.isEmpty()
                    && (!be.storedFluid.isFluidEqual(archFluid) || !be.storedFluid.isComponentEqual(archFluid))) {
                return null;
            }
            long space = TankBlockEntity.CAPACITY - be.amount;
            int toDrain = (int) Math.min(containedAmt, Math.min(space, Integer.MAX_VALUE));
            if (toDrain <= 0) return null;

            int drained;
            try (Transaction tx = Transaction.openRoot()) {
                drained = cap.extract(0, containedRes, toDrain, tx);
                if (drained > 0) tx.commit();
            }
            if (drained <= 0) return null;

            be.insert(FluidStack.create(archFluid, drained), drained, false);
            return tmp.getItem(0);

        } else {
            // Item is empty → fill from tank
            if (be.storedFluid.isEmpty() || be.amount == 0) return null;

            net.neoforged.neoforge.fluids.FluidStack neoStored =
                    FluidStackHooksForge.toForge(be.storedFluid.copyWithAmount(1));
            FluidResource storedRes = FluidResource.of(neoStored);

            if (!cap.isValid(0, storedRes)) return null;
            long capCapacity = cap.getCapacityAsLong(0, storedRes);
            int canFill = (int) Math.min(be.amount, Math.min(capCapacity, Integer.MAX_VALUE));
            if (canFill <= 0) return null;

            int actualInserted;
            try (Transaction tx = Transaction.openRoot()) {
                actualInserted = cap.insert(0, storedRes, canFill, tx);
                if (actualInserted > 0) {
                    be.extract(actualInserted, false);
                    tx.commit();
                }
            }
            if (actualInserted <= 0) return null;

            return tmp.getItem(0);
        }
    }

    public static void onCapabilitiesChanged(Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack)) != null;
    }
}
