package net.bobofraggins.mobfarmingsupplies.mixin;

import java.util.function.BiConsumer;
import net.bobofraggins.mobfarmingsupplies.soil.HyperFarmlandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A tree grown on Hyper Farmland leaves it as it is, instead of turning the block under its trunk
 * to dirt (26.x does this with the {@code cannot_replace_below_tree_trunk} tag).
 */
@Mixin(TrunkPlacer.class)
public abstract class TrunkPlacerHyperFarmlandMixin {

    @Inject(method = "setDirtAt", at = @At("HEAD"), cancellable = true)
    private static void mobfarmingsupplies$keepHyperFarmland(LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> setter, RandomSource random, BlockPos pos,
            TreeConfiguration config, CallbackInfo ci) {
        if (level.isStateAtPosition(pos, state -> state.getBlock() instanceof HyperFarmlandBlock)) ci.cancel();
    }
}
