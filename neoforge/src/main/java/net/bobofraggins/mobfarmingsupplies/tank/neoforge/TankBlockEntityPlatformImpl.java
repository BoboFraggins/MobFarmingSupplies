package net.bobofraggins.mobfarmingsupplies.tank.neoforge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

public final class TankBlockEntityPlatformImpl {

    private TankBlockEntityPlatformImpl() {}

    @Nullable
    public static ItemStack tryTransferFluidWithItem(TankBlockEntity be, ItemStack input) {
        IFluidHandlerItem cap = input.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (cap == null || cap.getTanks() == 0) return null;

        net.neoforged.neoforge.fluids.FluidStack contained = cap.getFluidInTank(0);

        if (!contained.isEmpty()) {
            // Item has fluid → drain into tank
            FluidStack archFluid = FluidStackHooksForge.fromForge(contained.copyWithAmount(1));
            if (!be.canHold(archFluid)) return null;
            if (!be.storedFluid.isEmpty()
                    && (!be.storedFluid.isFluidEqual(archFluid) || !be.storedFluid.isComponentEqual(archFluid))) {
                return null;
            }
            long space = Math.max(0, be.getCapacity() - be.amount);
            int toDrain = (int) Math.min(contained.getAmount(), Math.min(space, Integer.MAX_VALUE));
            if (toDrain <= 0) return null;

            int drained = cap.drain(contained.copyWithAmount(toDrain), IFluidHandler.FluidAction.EXECUTE).getAmount();
            if (drained <= 0) return null;

            be.insert(FluidStack.create(archFluid, drained), drained, false);
            return cap.getContainer();

        } else {
            // Item is empty → fill from tank
            if (be.storedFluid.isEmpty() || be.amount == 0) return null;

            int canFill = (int) Math.min(be.amount, Integer.MAX_VALUE);
            net.neoforged.neoforge.fluids.FluidStack offered =
                    FluidStackHooksForge.toForge(be.storedFluid.copyWithAmount(canFill));
            int filled = cap.fill(offered, IFluidHandler.FluidAction.EXECUTE);
            if (filled <= 0) return null;

            be.extract(filled, false);
            return cap.getContainer();
        }
    }

    public static void onCapabilitiesChanged(Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty()
                && stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM) != null;
    }
}
