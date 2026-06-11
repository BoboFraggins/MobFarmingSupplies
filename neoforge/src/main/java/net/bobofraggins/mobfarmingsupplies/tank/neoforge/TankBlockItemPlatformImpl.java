package net.bobofraggins.mobfarmingsupplies.tank.neoforge;

import net.bobofraggins.mobfarmingsupplies.tank.TankItemFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public final class TankBlockItemPlatformImpl {

    public static ItemStack tryPlaceFluidInWorld(
            ItemStack stack, Player player, Level level, InteractionHand hand, BlockPos targetPos) {
        TankItemFluidHandler tankHandler = new TankItemFluidHandler(stack.copy());
        net.neoforged.neoforge.fluids.FluidStack placed =
                FluidUtil.tryPlaceFluid(tankHandler, player, level, hand, targetPos);
        if (!placed.isEmpty()) {
            return tankHandler.getContainer();
        }
        return null;
    }

    public static boolean interactWithFluidBlock(
            Player player, InteractionHand hand, Level level, BlockPos pos, Direction face) {
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, face);
    }
}
