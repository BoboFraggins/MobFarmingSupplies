package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("UnstableApiUsage")
public final class TankBlockItemPlatformImpl {

    private static final int BUCKET_VOLUME = 1000;

    public static ItemStack tryPlaceFluidInWorld(
            ItemStack stack, Player player, Level level, InteractionHand hand, BlockPos pos) {
        TankContents c = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
        if (c.storedFluid().isEmpty() || c.amount() < BUCKET_VOLUME) return null;
        Fluid fluid = c.storedFluid().getFluid();
        if (!(fluid instanceof FlowingFluid flowing)) return null;

        BlockState state = level.getBlockState(pos);
        boolean placed;
        if (state.getBlock() instanceof LiquidBlockContainer lbc) {
            if (!lbc.canPlaceLiquid(player, level, pos, state, fluid)) return null;
            lbc.placeLiquid(level, pos, state, flowing.getSource(false));
            placed = true;
        } else if (level.isEmptyBlock(pos) || state.canBeReplaced()) {
            level.setBlock(pos, flowing.getSource(false).createLegacyBlock(), 11);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            placed = true;
        } else {
            return null;
        }

        if (placed) {
            ItemStack newStack = stack.copy();
            newStack.set(Registration.TANK_CONTENTS.get(),
                    new TankContents(c.storedFluid(), c.amount() - BUCKET_VOLUME, c.bucketMode()));
            return newStack;
        }
        return null;
    }

    public static boolean interactWithFluidBlock(
            Player player, InteractionHand hand, Level level, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        Storage<FluidVariant> storage =
                FluidStorage.SIDED.find(level, pos, state, level.getBlockEntity(pos), face);
        if (storage == null) return false;
        return FluidStorageUtil.interactWithFluidStorage(storage, player, hand);
    }
}
