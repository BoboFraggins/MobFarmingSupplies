package net.bobofraggins.mobfarmingsupplies.logisticsorter.fabric;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
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

    public static void pullPhase(LogisticSorterBlockEntity be, Level level, BlockPos pos) {
        if (!be.beginRouting()) return;
        try {
            for (Direction in : Direction.values()) {
                if (be.getSide(in) != SideMode.INPUT) continue;
                Storage<ItemVariant> source = storageAt(level, pos, in);
                if (source == null) continue;
                long budget = LogisticSorterBlockEntity.PULL_PER_SIDE;
                for (boolean matching : new boolean[]{true, false}) {
                    for (Direction out : be.outputs(matching)) {
                        if (budget <= 0) break;
                        Storage<ItemVariant> dest = storageAt(level, pos, out);
                        if (dest == null) continue;
                        try (Transaction tx = Transaction.openOuter()) {
                            long moved = StorageUtil.move(source, dest,
                                    v -> be.matches(v.toStack()) == matching, budget, tx);
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
            long inserted = 0;
            for (Direction out : be.outputsFor(variant.toStack())) {
                Storage<ItemVariant> dest = storageAt(level, be.getBlockPos(), out);
                if (dest == null) continue;
                inserted += dest.insert(variant, amount - inserted, tx);
                if (inserted >= amount) break;
            }
            return inserted;
        } finally {
            be.endRouting();
        }
    }

    @Nullable
    private static Storage<ItemVariant> storageAt(Level level, BlockPos pos, Direction side) {
        return ItemStorage.SIDED.find(level, pos.relative(side), side.getOpposite());
    }
}
