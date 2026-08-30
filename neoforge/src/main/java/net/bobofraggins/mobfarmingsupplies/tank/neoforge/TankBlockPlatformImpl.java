package net.bobofraggins.mobfarmingsupplies.tank.neoforge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.neoforge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public final class TankBlockPlatformImpl {

    public static boolean handleFluidItemInteraction(
            Player player, InteractionHand hand, Level level, BlockPos pos, boolean filling) {
        ResourceHandler<FluidResource> fluidCap =
                level.getCapability(Capabilities.Fluid.BLOCK, pos, null);
        if (fluidCap == null) return false;

        boolean success = FluidUtil.interactWithFluidHandler(player, hand, pos, fluidCap);
        if (success && level.getBlockEntity(pos) instanceof TankBlockEntity be) {
            FluidStack stored = be.getStoredFluid();
            if (!stored.isEmpty()) {
                var sound = FluidStackHooksForge.toForge(stored.copyWithAmount(1))
                        .getFluidType().getSound(filling ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
                if (sound == null) sound = filling ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        return success;
    }
}
