package net.bobofraggins.mobfarmingsupplies.logisticsorter.neoforge;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/** NeoForge implementation of {@link net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterPlatform}. */
public final class LogisticSorterPlatformImpl {

    private LogisticSorterPlatformImpl() {}

    public static void pullPhase(LogisticSorterBlockEntity be, Level level, BlockPos pos) {
        if (!be.beginRouting()) return;
        try {
            for (Direction in : Direction.values()) {
                if (be.getSide(in) != SideMode.INPUT) continue;
                ResourceHandler<ItemResource> source = handlerAt(level, pos, in);
                if (source == null) continue;
                int budget = LogisticSorterBlockEntity.PULL_PER_SIDE;
                for (boolean matching : new boolean[]{true, false}) {
                    for (Direction out : be.outputs(matching)) {
                        if (budget <= 0) break;
                        ResourceHandler<ItemResource> dest = handlerAt(level, pos, out);
                        if (dest == null) continue;
                        try (Transaction tx = Transaction.openRoot()) {
                            int moved = ResourceHandlerUtil.moveStacking(source, dest,
                                    r -> be.matches(r.toStack(1)) == matching, budget, tx);
                            if (moved > 0) {
                                tx.commit();
                                budget -= moved;
                            }
                        }
                    }
                }
            }
        } finally {
            be.endRouting();
        }
    }

    public static void invalidateCapabilities(Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    /**
     * Routes items pushed into an INPUT side straight to a matching destination, inside the
     * caller's transaction. Returns how many were accepted.
     */
    static int routeInsert(LogisticSorterBlockEntity be, ItemResource resource, int amount, TransactionContext tx) {
        Level level = be.getLevel();
        if (level == null || resource.isEmpty() || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0; // sorters feeding each other in a loop
        try {
            int inserted = 0;
            for (Direction out : be.outputsFor(resource.toStack(1))) {
                ResourceHandler<ItemResource> dest = handlerAt(level, be.getBlockPos(), out);
                if (dest == null) continue;
                inserted += dest.insert(resource, amount - inserted, tx);
                if (inserted >= amount) break;
            }
            return inserted;
        } finally {
            be.endRouting();
        }
    }

    @Nullable
    private static ResourceHandler<ItemResource> handlerAt(Level level, BlockPos pos, Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos.relative(side), side.getOpposite());
    }
}
