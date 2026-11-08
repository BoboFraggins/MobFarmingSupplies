package net.bobofraggins.mobfarmingsupplies.wardeninhibitor;

import net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Warden Inhibitor: the Wither Inhibitor's counterpart for Wardens. Mounted the same way, with
 * the same area, menu and smoke, it keeps any Warden in its area from using its sonic boom or
 * giving players Darkness (see {@link WardenInhibitorEvents}).
 */
public class WardenInhibitorBlock extends WitherInhibitorBlock {

    public WardenInhibitorBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardenInhibitorBlockEntity(pos, state);
    }
}
