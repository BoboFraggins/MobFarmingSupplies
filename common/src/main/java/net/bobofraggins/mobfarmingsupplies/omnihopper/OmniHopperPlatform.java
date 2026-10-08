package net.bobofraggins.mobfarmingsupplies.omnihopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Loader-specific resource movement for the Omnidirectional Hopper.
 *
 * <p>NeoForge impl: items, fluids, energy and Mekanism chemicals via NeoForge's Transfer API
 * capabilities. Fabric impl: items and fluids via Fabric's Transfer API. Each loader also exposes
 * insert-only handlers on the hopper's INPUT sides that route pushed-in resources straight on to
 * an OUTPUT neighbour.
 */
public final class OmniHopperPlatform {

    private OmniHopperPlatform() {}

    /**
     * Pulls from each INPUT side and pushes to the OUTPUT sides, split evenly. Items only move on
     * {@code itemTick}; everything else moves every tick.
     */
    @ExpectPlatform
    public static void transfer(OmniHopperBlockEntity be, Level level, BlockPos pos, boolean itemTick) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Tells the platform that the hopper's exposed handlers changed (its INPUT sides moved). */
    @ExpectPlatform
    public static void invalidateCapabilities(Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }
}
