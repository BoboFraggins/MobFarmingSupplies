package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class AbsorptionHopperBlockEntityPlatform {

    private AbsorptionHopperBlockEntityPlatform() {}

    /**
     * Pushes from the hopper to every adjacent side whose bit is set in
     * {@link AbsorptionHopperBlockEntity#getPushSides()}: up to the hoppers' items-per-transfer to
     * each side when {@code itemTick}, and up to their fluid-per-tick of XP Juice every call.
     *
     * <p>NeoForge impl: uses {@code Capabilities.Item/Fluid.BLOCK} + NeoForge Transfer API.
     * Fabric impl: uses {@code ItemStorage.SIDED} / {@code FluidStorage.SIDED}.
     */
    @ExpectPlatform
    public static void outputPhase(AbsorptionHopperBlockEntity be, Level level, BlockPos pos, boolean itemTick) {
        throw new AssertionError("Missing platform implementation");
    }
}
