package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class AbsorptionHopperBlockEntityPlatform {

    private AbsorptionHopperBlockEntityPlatform() {}

    /*
     * Both push to every adjacent side whose bit is set in
     * AbsorptionHopperBlockEntity#getPushSides(), once per transfer (see TransferSchedule).
     * NeoForge impl: Capabilities.Item/Fluid.BLOCK + NeoForge Transfer API.
     * Fabric impl: ItemStorage.SIDED / FluidStorage.SIDED.
     */

    /** Pushes up to the hoppers' items-per-transfer to each side. Returns whether anything moved. */
    @ExpectPlatform
    public static boolean pushItems(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Pushes one transfer's worth of XP Juice to each side. Returns whether anything moved. */
    @ExpectPlatform
    public static boolean pushFluid(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }
}
