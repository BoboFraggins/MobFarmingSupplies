package net.bobofraggins.mobfarmingsupplies.blockbreaker.neoforge;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class BlockBreakerPlatformImpl {

    private BlockBreakerPlatformImpl() {}

    public static boolean deliverAll(Level level, BlockPos pos, List<Direction> outputs, List<ItemStack> stacks) {
        try (Transaction tx = Transaction.openRoot()) {
            for (ItemStack stack : stacks) {
                int left = stack.getCount();
                ItemResource resource = ItemResource.of(stack);
                for (Direction side : outputs) {
                    if (left <= 0) break;
                    ResourceHandler<ItemResource> dest =
                            level.getCapability(Capabilities.Item.BLOCK, pos.relative(side), side.getOpposite());
                    if (dest != null) left -= ResourceHandlerUtil.insertStacking(dest, resource, left, tx);
                }
                if (left > 0) return false; // rolled back: the transaction is never committed
            }
            tx.commit();
            return true;
        }
    }
}
