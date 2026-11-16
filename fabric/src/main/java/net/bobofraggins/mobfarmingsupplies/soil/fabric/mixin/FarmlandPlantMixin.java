package net.bobofraggins.mobfarmingsupplies.soil.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.soil.HyperFarmlandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Crops, stems, pitcher plants, saplings and flowers can be planted on Hyper Farmland as on
 * farmland. Vanilla checks for {@code Blocks.FARMLAND} itself; NeoForge checks for any
 * {@code FarmBlock}, which Hyper Farmland is, so this is Fabric only.
 */
@Mixin({CropBlock.class, StemBlock.class, AttachedStemBlock.class, PitcherCropBlock.class, BushBlock.class})
public abstract class FarmlandPlantMixin {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$onHyperFarmland(BlockState state, BlockGetter level, BlockPos pos,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof HyperFarmlandBlock) cir.setReturnValue(true);
    }
}
