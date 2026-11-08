package net.bobofraggins.mobfarmingsupplies.shared.ui.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SideGridPlatformImpl {

    private SideGridPlatformImpl() {}

    public static ItemStack pickBlock(Level level, BlockPos pos, BlockState state, Player player) {
        return state.getCloneItemStack(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false), level, pos, player);
    }
}
