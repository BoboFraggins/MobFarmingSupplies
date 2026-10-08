package net.bobofraggins.mobfarmingsupplies.shared.sides;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/**
 * Block item for a block with a side-configuration grid: records the placed block's orientation
 * (see {@link SideLayout#fromPlacement}). Runs after any saved block entity data is applied, so a
 * picked-up block that's placed again also follows the placement rules.
 */
public class OrientedBlockItem extends BlockItem {

    public OrientedBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result.consumesAction() && !ctx.getLevel().isClientSide()
                && ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof SideOriented oriented) {
            oriented.setSideOrientation(SideLayout.fromPlacement(ctx));
        }
        return result;
    }
}
