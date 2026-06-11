package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TankBlockPlatform {

    private TankBlockPlatform() {}

    /**
     * Handles bucket/fluid-container interaction against the tank block — acquires
     * the block's fluid capability, transfers fluid, and plays the bucket sound.
     *
     * @param filling true when the held item is filling the tank (bucket → tank direction)
     * @return true if any fluid was transferred
     */
    @ExpectPlatform
    public static boolean handleFluidItemInteraction(
            Player player, InteractionHand hand, Level level, BlockPos pos, boolean filling) {
        throw new AssertionError("Missing platform implementation");
    }
}
