package net.bobofraggins.mobfarmingsupplies.logisticsorter.fabric;

import net.bobofraggins.mobfarmingsupplies.transfer.fabric.HopperLookups;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Fabric implementation of {@link net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterPlatform}. */
@SuppressWarnings("UnstableApiUsage")
public final class LogisticSorterPlatformImpl {

    private LogisticSorterPlatformImpl() {}

    public static long pullPhase(LogisticSorterBlockEntity be, Level level, BlockPos pos) {
        if (!be.beginRouting()) return 0;
        long total = 0;
        try {
            for (Direction in : Direction.values()) {
                if (be.getSide(in) != SideMode.INPUT) continue;
                Storage<ItemVariant> source = storageAt(be, level, pos, in);
                if (source == null) continue;
                long budget = MFSConfig.getHopperItemsPerTransfer();
                for (boolean matching : new boolean[]{true, false}) {
                    if (budget <= 0) break;
                    // Split what the source can actually give, not the whole budget - otherwise
                    // 9 items over two outputs would all land in the first one's share of 32.
                    // Nothing to move: skip without opening a transaction.
                    if (!HopperLookups.hasAny(source, v -> be.matches(v.toStack()) == matching)) continue;
                    long amount = available(source, be, matching, budget);
                    if (amount <= 0) continue;
                    long sent = EvenSplit.distribute(be.outputs(matching), amount, (out, max) -> {
                        Storage<ItemVariant> dest = storageAt(be, level, pos, out);
                        if (dest == null || HopperLookups.definitelyFull(dest)) return 0;
                        try (Transaction tx = Transaction.openOuter()) {
                            long moved = StorageUtil.move(source, dest,
                                    v -> be.matches(v.toStack()) == matching, max, tx);
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
    private static long available(Storage<ItemVariant> source, LogisticSorterBlockEntity be, boolean matching, long max) {
        long found = 0;
        try (Transaction simulation = Transaction.openOuter()) { // never committed
            for (StorageView<ItemVariant> view : source.nonEmptyViews()) {
                ItemVariant variant = view.getResource();
                if (be.matches(variant.toStack()) != matching) continue;
                found += view.extract(variant, max - found, simulation);
                if (found >= max) break;
            }
        }
        return found;
    }

    public static void invalidateCapabilities(Level level, BlockPos pos) {
        // Fabric's block API lookup isn't cached per side here — nothing to invalidate.
    }

    /**
     * Routes items pushed into an INPUT side straight to a matching destination, inside the
     * caller's transaction. Returns how many were accepted.
     */
    static long routeInsert(LogisticSorterBlockEntity be, ItemVariant variant, long amount, TransactionContext tx) {
        Level level = be.getLevel();
        if (level == null || variant.isBlank() || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0; // sorters feeding each other in a loop
        try {
            return EvenSplit.distribute(be.outputsFor(variant.toStack()), amount, (out, max) -> {
                Storage<ItemVariant> dest = storageAt(be, level, be.getBlockPos(), out);
                return dest == null ? 0 : dest.insert(variant, max, tx);
            });
        } finally {
            be.endRouting();
        }
    }

    /** The item storage beside {@code pos} on {@code side}, from the sorter's cached lookups. */
    @Nullable
    private static Storage<ItemVariant> storageAt(LogisticSorterBlockEntity be, Level level, BlockPos pos, Direction side) {
        return HopperLookups.of(be).at(level, pos, side, ItemStorage.SIDED);
    }
}
