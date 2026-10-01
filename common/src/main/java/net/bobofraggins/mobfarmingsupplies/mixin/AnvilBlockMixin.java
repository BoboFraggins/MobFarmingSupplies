package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.crushing.AnvilCrushing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Anvil crushing: a falling anvil converts the items it lands on (see {@link AnvilCrushing}).
 * Neither loader has a "falling block landed" event, so this hooks the anvil directly — both
 * when it lands and when it breaks on landing.
 */
@Mixin(AnvilBlock.class)
public abstract class AnvilBlockMixin {

    @Inject(method = "onLand", at = @At("HEAD"))
    private void mobfarmingsupplies$crushOnLand(Level level, BlockPos pos, BlockState state,
            BlockState replacedBlock, FallingBlockEntity fallingBlock, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) AnvilCrushing.crush(serverLevel, pos);
    }

    @Inject(method = "onBrokenAfterFall", at = @At("HEAD"))
    private void mobfarmingsupplies$crushOnBreak(Level level, BlockPos pos, FallingBlockEntity fallingBlock,
            CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) AnvilCrushing.crush(serverLevel, pos);
    }
}
