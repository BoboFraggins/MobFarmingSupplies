package net.bobofraggins.mobfarmingsupplies.tank.neoforge;

import net.bobofraggins.mobfarmingsupplies.neoforge.tank.TankItemFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class TankBlockItemPlatformImpl {

    public static ItemStack tryPlaceFluidInWorld(
            ItemStack stack, Player player, Level level, InteractionHand hand, BlockPos targetPos) {
        TankItemFluidHandler tankHandler = new TankItemFluidHandler(stack.copy());
        FluidStack bucket = tankHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (bucket.getAmount() < FluidType.BUCKET_VOLUME) return null;
        if (FluidUtil.tryPlaceFluid(player, level, hand, targetPos, tankHandler, bucket)) {
            return tankHandler.getContainer();
        }
        return null;
    }

    public static boolean interactWithFluidBlock(
            Player player, InteractionHand hand, Level level, BlockPos pos, Direction face) {
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, face);
    }
}
