package net.bobofraggins.mobfarmingsupplies.omnihopper.fabric;

import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
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

    /** Fabric measures fluids in droplets: 81 per mB. */
    private static final long DROPLETS_PER_MB = 81L;

    private OmniHopperPlatformImpl() {}

    public static void transfer(HopperNode node, boolean itemTick) {
        Level level = node.getLevel();
        if (level == null || !node.beginRouting()) return;
        try {
            BlockPos pos = node.getBlockPos();
            if (itemTick) {
                Predicate<ItemVariant> items = v -> node.allowsItem(v.toStack());
                moveResources(node, level, pos, ItemStorage.SIDED, items, MFSConfig.getHopperItemsPerTransfer());
            }
            // The mod counts fluid in mB; Fabric's Transfer API counts droplets.
            moveResources(node, level, pos, FluidStorage.SIDED, v -> true,
                    MFSConfig.getHopperFluidPerTick() * DROPLETS_PER_MB);
        } finally {
            node.endRouting();
        }
    }

    public static void invalidateCapabilities(Level level, BlockPos pos) {
        // Fabric's block API lookup isn't cached per side here — nothing to invalidate.
    }

    /** Moves up to {@code perSide} from each INPUT neighbour, split evenly across the node's OUTPUT targets. */
    private static <T extends TransferVariant<?>> void moveResources(
            HopperNode be, Level level, BlockPos pos,
            BlockApiLookup<Storage<T>, Direction> lookup, Predicate<T> filter, long perSide) {
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            Storage<T> source = storageAt(level, pos, in, lookup);
            if (source == null) continue;
            // Split what the source can actually give, not the whole budget - otherwise 9 items
            // over two outputs would all land in the first one's share of 32.
            long amount = available(source, filter, perSide);
            if (amount <= 0) continue;
            EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                Storage<T> dest = storageAt(out, lookup);
                if (dest == null) return 0;
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
                Storage<T> dest = storageAt(out, lookup);
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

    @Nullable
    private static <T> Storage<T> storageAt(Level level, BlockPos pos, Direction side,
                                            BlockApiLookup<Storage<T>, Direction> lookup) {
        BlockPos neighbour = pos.relative(side);
        // Never load a chunk just to look: a bridge's targets can be anywhere.
        if (!level.isLoaded(neighbour)) return null;
        return lookup.find(level, neighbour, side.getOpposite());
    }

    @Nullable
    private static <T> Storage<T> storageAt(HopperOutput out, BlockApiLookup<Storage<T>, Direction> lookup) {
        return storageAt(out.level(), out.pos(), out.side(), lookup);
    }
}
