package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Loader-specific item movement for the Logistic Sorter.
 *
 * <p>NeoForge impl: {@code Capabilities.Item.BLOCK} + NeoForge Transfer API.
 * Fabric impl: {@code ItemStorage.SIDED} + Fabric Transfer API.
 * Each loader also registers an insert-only handler on the sorter's INPUT sides that routes
 * pushed-in items straight to a destination.
 */
public final class LogisticSorterPlatform {

    private LogisticSorterPlatform() {}

    /**
     * Pulls items from the inventory on each INPUT side and moves them to a MATCH side
     * (items passing the filters) or a NO_MATCH side (the rest), up to
     * {@link LogisticSorterBlockEntity#PULL_PER_SIDE} items per INPUT side.
     */
    @ExpectPlatform
    public static void pullPhase(LogisticSorterBlockEntity be, Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Tells the platform that the sorter's exposed handlers changed (its INPUT sides moved). */
    @ExpectPlatform
    public static void invalidateCapabilities(Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }
}
