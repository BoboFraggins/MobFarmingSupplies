package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class TankBlockItemPlatform {

    private TankBlockItemPlatform() {}

    /**
     * Tries to place one bucket (1 000 mB) of fluid from the tank item into the world
     * at {@code targetPos}.
     *
     * @return the updated item stack (fluid reduced by one bucket) on success, or
     *         {@code null} if placement failed
     */
    @ExpectPlatform
    public static ItemStack tryPlaceFluidInWorld(
            ItemStack stack, Player player, Level level, InteractionHand hand, BlockPos targetPos) {
        throw new AssertionError("Missing platform implementation");
    }

    /**
     * Transfers fluid between the tank item in {@code hand} and the fluid-capable block
     * at {@code pos}/{@code face}.
     *
     * @return true if any fluid was transferred
     */
    @ExpectPlatform
    public static boolean interactWithFluidBlock(
            Player player, InteractionHand hand, Level level, BlockPos pos, Direction face) {
        throw new AssertionError("Missing platform implementation");
    }
}
