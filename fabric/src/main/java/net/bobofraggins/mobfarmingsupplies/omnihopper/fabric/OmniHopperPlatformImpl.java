package net.bobofraggins.mobfarmingsupplies.omnihopper.fabric;

import net.bobofraggins.mobfarmingsupplies.transfer.fabric.HopperLookups;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.fluid.fabric.FabricFluidUnits;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperOutput;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of {@link net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperPlatform}:
 * items and fluids only (Fabric itself has no energy API, and Mekanism is NeoForge-only).
 */
@SuppressWarnings("UnstableApiUsage")
public final class OmniHopperPlatformImpl {

    private OmniHopperPlatformImpl() {}

    public static void transfer(HopperNode node) {
        Level level = node.getLevel();
        if (level == null || !node.beginRouting()) return;
        try {
            BlockPos pos = node.getBlockPos();
            TransferSchedule schedule = node.transferSchedule();
            Predicate<ItemVariant> items = v -> node.allowsItem(v.toStack());
            schedule.attempt(TransferSchedule.Kind.ITEMS, () -> moveResources(node, level, pos,
                    ItemStorage.SIDED, items, MFSConfig.getHopperItemsPerTransfer()) > 0);
            // The mod counts fluid in mB; Fabric's Transfer API counts droplets.
            schedule.attempt(TransferSchedule.Kind.FLUIDS, () -> moveResources(node, level, pos,
                    FluidStorage.SIDED, v -> true,
                    FabricFluidUnits.toDroplets(TransferSchedule.perTransfer(MFSConfig.getHopperFluidPerTick()))) > 0);
        } finally {
            node.endRouting();
        }
    }

    public static void invalidateCapabilities(Level level, BlockPos pos) {
        // Fabric's block API lookup isn't cached per side here — nothing to invalidate.
    }

    /**
     * Moves up to {@code perSide} from each INPUT neighbour, split evenly across the node's OUTPUT
     * targets. Returns how much moved.
     */
    private static <T extends TransferVariant<?>> long moveResources(
            HopperNode be, Level level, BlockPos pos,
            BlockApiLookup<Storage<T>, Direction> lookup, Predicate<T> filter, long perSide) {
        long total = 0;
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            Storage<T> source = storageAt(be, level, pos, in, lookup);
            // Nothing to move: skip without opening a transaction.
            if (source == null || !HopperLookups.hasAny(source, filter)) continue;
            // Split what the source can actually give, not the whole budget - otherwise 9 items
            // over two outputs would all land in the first one's share of 32.
            long amount = available(source, filter, perSide);
            if (amount <= 0) continue;
            total += EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                Storage<T> dest = storageAt(be, out, lookup);
                if (dest == null || HopperLookups.definitelyFull(dest)) return 0;
                try (Transaction tx = Transaction.openOuter()) {
                    long moved = StorageUtil.move(source, dest, filter, max, tx);
                    if (moved > 0) {
                        tx.commit();
                        be.delivered(out);
                    }
                    return moved;
                }
            });
        }
        return total;
    }

    /** How much (up to {@code max}) {@code source} can give that passes {@code filter}. */
    private static <T extends TransferVariant<?>> long available(Storage<T> source, Predicate<T> filter, long max) {
        long found = 0;
        try (Transaction simulation = Transaction.openOuter()) { // never committed
            for (StorageView<T> view : source.nonEmptyViews()) {
                T variant = view.getResource();
                if (!filter.test(variant)) continue;
                found += view.extract(variant, max - found, simulation);
                if (found >= max) break;
            }
        }
        return found;
    }

    /**
     * Routes a resource pushed into an INPUT side straight to the node's OUTPUT targets, inside the
     * caller's transaction. Returns how much was accepted.
     */
    static <T extends TransferVariant<?>> long routeInsert(HopperNode be,
            BlockApiLookup<Storage<T>, Direction> lookup, T variant, long amount, TransactionContext tx) {
        Level level = be.getLevel();
        if (level == null || variant.isBlank() || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0; // hoppers feeding each other in a loop
        try {
            return EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                Storage<T> dest = storageAt(be, out, lookup);
                long inserted = dest == null ? 0 : dest.insert(variant, max, tx);
                if (inserted > 0) {
                    tx.addOuterCloseCallback(result -> { if (result.wasCommitted()) be.delivered(out); });
                }
                return inserted;
            });
        } finally {
            be.endRouting();
        }
    }

    /** The storage beside {@code pos} on {@code side}, from the node's cached lookups. */
    @Nullable
    private static <T> Storage<T> storageAt(HopperNode node, Level level, BlockPos pos, Direction side,
                                            BlockApiLookup<Storage<T>, Direction> lookup) {
        return HopperLookups.of(node).at(level, pos, side, lookup);
    }

    @Nullable
    private static <T> Storage<T> storageAt(HopperNode node, HopperOutput out, BlockApiLookup<Storage<T>, Direction> lookup) {
        return storageAt(node, out.level(), out.pos(), out.side(), lookup);
    }
}
