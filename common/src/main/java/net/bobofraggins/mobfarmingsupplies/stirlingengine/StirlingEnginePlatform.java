package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** The Stirling Engine's energy: FE on NeoForge, Team Reborn Energy on Fabric. */
public final class StirlingEnginePlatform {

    private StirlingEnginePlatform() {}

    /**
     * Offers up to {@code perSide} energy to each neighbour of {@code pos} that accepts energy,
     * while it lasts, and returns how much they took in total.
     */
    @ExpectPlatform
    public static long pushEnergy(Level level, BlockPos pos, long perSide) {
        throw new AssertionError();
    }
}
