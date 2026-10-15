package net.bobofraggins.mobfarmingsupplies.transfer.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric lookups and quick checks for the hoppers' transfer code. Each hopper keeps one of these
 * in its {@link TransferSchedule}: it holds a {@link BlockApiCache} per neighbour and lookup, so a
 * transfer doesn't search for the neighbour's storage again. Fabric keeps the caches up to date
 * itself when a neighbour changes.
 *
 * <p>The static checks let a transfer skip work that can't move anything, without opening a
 * transaction.
 */
public final class HopperLookups {

    /** More cached neighbours than this and the map is cleared (a bridge's targets can drift). */
    private static final int MAX_CACHED = 128;

    private record Key(BlockApiLookup<?, Direction> lookup, ResourceKey<Level> dimension, BlockPos pos) {}

    private final Map<Key, BlockApiCache<?, Direction>> caches = new HashMap<>();

    /** The lookups of a hopper. */
    public static HopperLookups of(TransferSchedule.Holder hopper) {
        return hopper.transferSchedule().lookups(HopperLookups::new);
    }

    /**
     * The storage {@code lookup} finds on the block on {@code side} of {@code pos}, facing back at
     * it; null if there is none or its chunk isn't loaded (never loads a chunk just to look).
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <S> S at(Level level, BlockPos pos, Direction side, BlockApiLookup<S, Direction> lookup) {
        BlockPos neighbour = pos.relative(side);
        if (!level.isLoaded(neighbour)) return null;
        if (!(level instanceof ServerLevel server)) return lookup.find(level, neighbour, side.getOpposite());
        Key key = new Key(lookup, level.dimension(), neighbour);
        BlockApiCache<?, Direction> cache = caches.get(key);
        if (cache == null) {
            if (caches.size() >= MAX_CACHED) caches.clear();
            cache = BlockApiCache.create(lookup, server, neighbour);
            caches.put(key, cache);
        }
        return (S) cache.find(side.getOpposite());
    }

    /** Whether {@code source} holds anything {@code filter} lets through. Opens no transaction. */
    public static <T> boolean hasAny(Storage<T> source, Predicate<T> filter) {
        for (StorageView<T> view : source.nonEmptyViews()) {
            if (filter.test(view.getResource())) return true;
        }
        return false;
    }

    /**
     * Whether {@code dest} certainly can't take any more: it takes nothing at all, or every view
     * holds something and is at its capacity. A storage that lists no views is left to the
     * transfer to decide.
     */
    public static <T> boolean definitelyFull(Storage<T> dest) {
        if (!dest.supportsInsertion()) return true;
        boolean anyView = false;
        for (StorageView<T> view : dest) {
            anyView = true;
            if (view.isResourceBlank() || view.getAmount() < view.getCapacity()) return false;
        }
        return anyView;
    }
}
