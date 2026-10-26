package net.bobofraggins.mobfarmingsupplies.blockbreaker;

import dev.architectury.injectables.annotations.ExpectPlatform;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Loader-specific item delivery for the Block Breaker (NeoForge's and Fabric's transfer APIs). */
public final class BlockBreakerPlatform {

    private BlockBreakerPlatform() {}

    /**
     * Puts all of {@code stacks} into the neighbours of {@code pos} on the {@code outputs} sides,
     * trying the sides in order for each stack, all in one transaction: if anything doesn't fit,
     * nothing is moved. Returns whether everything went.
     */
    @ExpectPlatform
    public static boolean deliverAll(Level level, BlockPos pos, List<Direction> outputs, List<ItemStack> stacks) {
        throw new AssertionError("Missing platform implementation");
    }
}
