package net.bobofraggins.mobfarmingsupplies.shared.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a {@link StateBlockEntityRenderer} draws from, copied out of the block entity each frame.
 * Mirrors the render state later versions pass to block entity renderers, so renderer bodies stay
 * the same across branches.
 */
public class BlockEntityRenderState {
    public BlockPos blockPos = BlockPos.ZERO;
    public BlockState blockState = Blocks.AIR.defaultBlockState();
    public int lightCoords;
    public int overlayCoords;

    /** Copies what every renderer needs; renderers add their own fields after calling this. */
    public static void extractBase(BlockEntity be, BlockEntityRenderState state, int light, int overlay) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.lightCoords = light;
        state.overlayCoords = overlay;
    }
}
