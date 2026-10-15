package net.bobofraggins.mobfarmingsupplies.logisticsorter.neoforge;

import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.HopperLookups;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Capabilities;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandlerUtil;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/** NeoForge implementation of {@link net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterPlatform}. */
public final class LogisticSorterPlatformImpl {

    private LogisticSorterPlatformImpl() {}

    public static long pullPhase(LogisticSorterBlockEntity be, Level level, BlockPos pos) {
        if (!be.beginRouting()) return 0;
        long total = 0;
        try {
            for (Direction in : Direction.values()) {
                if (be.getSide(in) != SideMode.INPUT) continue;
                ResourceHandler<ItemResource> source = handlerAt(be, level, pos, in);
                if (source == null) continue;
                long budget = MFSConfig.getHopperItemsPerTransfer();
                for (boolean matching : new boolean[]{true, false}) {
                    if (budget <= 0) break;
                    // Split what the source can actually give, not the whole budget - otherwise
                    // 9 items over two outputs would all land in the first one's share of 32.
                    // Nothing to move: skip without opening a transaction.
                    if (!HopperLookups.hasAny(source, r -> be.matches(r.toStack(1)) == matching)) continue;
                    int amount = available(source, be, matching, (int) budget);
                    if (amount <= 0) continue;
                    long sent = EvenSplit.distribute(be.outputs(matching), amount, (out, max) -> {
                        ResourceHandler<ItemResource> dest = handlerAt(be, level, pos, out);
                        if (dest == null || HopperLookups.definitelyFull(dest)) return 0;
                        try (Transaction tx = Transaction.openRoot()) {
                            int moved = ResourceHandlerUtil.moveStacking(source, dest,
                                    r -> be.matches(r.toStack(1)) == matching, (int) max, tx);
                            if (moved > 0) tx.commit();
                            return moved;
                        }
                    });
                    budget -= sent;
                    total += sent;
                }
            }
        } finally {
            be.endRouting();
        }
        return total;
    }

    /** How many items (up to {@code max}) {@code source} can give that match or don't match the filters. */
    private static int available(ResourceHandler<ItemResource> source, LogisticSorterBlockEntity be, boolean matching, int max) {
        int found = 0;
        try (Transaction simulation = Transaction.openRoot()) { // never committed
            for (int slot = 0; slot < source.size() && found < max; slot++) {
                ItemResource resource = source.getResource(slot);
                if (resource.isEmpty() || be.matches(resource.toStack(1)) != matching) continue;
                found += source.extract(slot, resource, max - found, simulation);
            }
        }
        return found;
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
            return (int) EvenSplit.distribute(be.outputsFor(resource.toStack(1)), amount, (out, max) -> {
                ResourceHandler<ItemResource> dest = handlerAt(be, level, be.getBlockPos(), out);
                return dest == null ? 0 : dest.insert(resource, (int) max, tx);
            });
        } finally {
            be.endRouting();
        }
    }

    /** The item handler beside {@code pos} on {@code side}, from the sorter's cached lookups. */
    @Nullable
    private static ResourceHandler<ItemResource> handlerAt(LogisticSorterBlockEntity be, Level level, BlockPos pos,
                                                           Direction side) {
        return HopperLookups.of(be).at(level, pos, side, Capabilities.Item.BLOCK);
    }
}
