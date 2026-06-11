package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class AbsorptionHopperBlockEntityPlatform {

    private AbsorptionHopperBlockEntityPlatform() {}

    /**
     * Pushes items and fluid from the hopper to every adjacent side whose bit is set in
     * {@link AbsorptionHopperBlockEntity#getPushSides()}.
     *
     * <p>NeoForge impl: uses {@code Capabilities.Item/Fluid.BLOCK} + NeoForge Transfer API.
     * Fabric impl: uses {@code ItemStorage.SIDED} / {@code FluidStorage.SIDED}.
     */
    @ExpectPlatform
    public static void outputPhase(AbsorptionHopperBlockEntity be, Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }
}
