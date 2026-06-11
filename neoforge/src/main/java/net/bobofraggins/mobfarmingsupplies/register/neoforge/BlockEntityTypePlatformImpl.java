package net.bobofraggins.mobfarmingsupplies.register.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public final class BlockEntityTypePlatformImpl {

    public static <T extends BlockEntity> BlockEntityType<T> create(
            BiFunction<BlockPos, BlockState, T> factory, Block block) {
        // NeoForge's patched jar has a public BlockEntityType constructor
        return new BlockEntityType<>((pos, state) -> factory.apply(pos, state), block);
    }
}
