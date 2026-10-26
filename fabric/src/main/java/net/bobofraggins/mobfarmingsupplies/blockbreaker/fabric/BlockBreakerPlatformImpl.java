package net.bobofraggins.mobfarmingsupplies.blockbreaker.fabric;

import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
public final class BlockBreakerPlatformImpl {

    private BlockBreakerPlatformImpl() {}

    public static boolean deliverAll(Level level, BlockPos pos, List<Direction> outputs, List<ItemStack> stacks) {
        try (Transaction tx = Transaction.openOuter()) {
            for (ItemStack stack : stacks) {
                long left = stack.getCount();
                ItemVariant variant = ItemVariant.of(stack);
                for (Direction side : outputs) {
                    if (left <= 0) break;
                    Storage<ItemVariant> dest = ItemStorage.SIDED.find(level, pos.relative(side), side.getOpposite());
                    if (dest != null) left -= dest.insert(variant, left, tx);
                }
                if (left > 0) return false; // rolled back: the transaction is never committed
            }
            tx.commit();
            return true;
        }
    }
}
