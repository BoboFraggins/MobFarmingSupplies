package net.bobofraggins.mobfarmingsupplies.register;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public final class BlockEntityTypePlatform {

    private BlockEntityTypePlatform() {}

    /**
     * Creates a BlockEntityType for the given factory and block.
     * Uses BiFunction to avoid referencing BlockEntityType.BlockEntitySupplier, which is
     * package-private in vanilla MC (Fabric) but public in NeoForge's patched jar.
     */
    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> create(
            BiFunction<BlockPos, BlockState, T> factory, Block block) {
        throw new AssertionError("Missing platform implementation");
    }
}
