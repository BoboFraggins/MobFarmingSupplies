package net.bobofraggins.mobfarmingsupplies.shared.ui.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class SideGridPlatformImpl {

    private SideGridPlatformImpl() {}

    public static ItemStack pickBlock(Level level, BlockPos pos, BlockState state, Player player) {
        return state.getBlock().getCloneItemStack(level, pos, state);
    }
}
