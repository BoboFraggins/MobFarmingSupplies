package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public final class TankBlockPlatformImpl {

    public static boolean handleFluidItemInteraction(
            Player player, InteractionHand hand, Level level, BlockPos pos, boolean filling) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, be, null);
        if (storage == null) return false;

        boolean success = FluidStorageUtil.interactWithFluidStorage(storage, player, hand);
        if (success) {
            level.playSound(null, pos,
                    filling ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return success;
    }
}
