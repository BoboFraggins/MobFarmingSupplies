package net.bobofraggins.mobfarmingsupplies.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bobofraggins.mobfarmingsupplies.soil.HyperFarmlandBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sugar cane can be planted on Hyper Farmland, as on dirt (it still needs water beside it). 1.21.1
 * has no {@code supports_sugar_cane} tag, and putting the farmland in {@code #minecraft:dirt}
 * would also let endermen carry it off and moss and sculk spread onto it.
 */
@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneHyperFarmlandMixin {

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean mobfarmingsupplies$onHyperFarmland(BlockState below, TagKey<Block> tag, Operation<Boolean> original) {
        return original.call(below, tag) || (tag == BlockTags.DIRT && below.getBlock() instanceof HyperFarmlandBlock);
    }
}
