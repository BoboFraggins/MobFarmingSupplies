package net.bobofraggins.mobfarmingsupplies.soil.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bobofraggins.mobfarmingsupplies.soil.HyperFarmlandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Crops grow on Hyper Farmland as on watered farmland: vanilla's growth speed only counts
 * {@code Blocks.FARMLAND} (NeoForge asks the block, so this is Fabric only).
 */
@Mixin(CropBlock.class)
public abstract class CropGrowthSpeedMixin {

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean mobfarmingsupplies$hyperFarmlandIsFarmland(BlockState state, Block block,
                                                                      Operation<Boolean> original) {
        return original.call(state, block) || (block == Blocks.FARMLAND && state.getBlock() instanceof HyperFarmlandBlock);
    }
}
