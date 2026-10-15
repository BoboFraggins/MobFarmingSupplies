package net.bobofraggins.mobfarmingsupplies.neoforge.transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge lookups and quick checks for the hoppers' transfer code. Each hopper keeps one of these
 * in its {@link TransferSchedule}: it holds a {@link BlockCapabilityCache} per neighbour and
 * capability, so a transfer doesn't look the neighbour's handler up again. NeoForge invalidates
 * the caches itself when a neighbour changes.
 *
 * <p>The static checks let a transfer skip work that can't move anything, without opening a
 * transaction.
 */
public final class HopperLookups {

    /** More cached neighbours than this and the map is cleared (a bridge's targets can drift). */
    private static final int MAX_CACHED = 128;

    private record Key(BlockCapability<?, Direction> capability, ResourceKey<Level> dimension, BlockPos pos,
                       Direction side) {}

    private final Map<Key, BlockCapabilityCache<?, Direction>> caches = new HashMap<>();

    /** The lookups of a hopper. */
    public static HopperLookups of(TransferSchedule.Holder hopper) {
        return hopper.transferSchedule().lookups(HopperLookups::new);
    }

    /**
     * The {@code capability} of the block on {@code side} of {@code pos}, facing back at it; null if
     * there is none or its chunk isn't loaded (never loads a chunk just to look).
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <H> H at(Level level, BlockPos pos, Direction side, BlockCapability<H, Direction> capability) {
        BlockPos neighbour = pos.relative(side);
        if (!level.isLoaded(neighbour)) return null;
        if (!(level instanceof ServerLevel server)) return level.getCapability(capability, neighbour, side.getOpposite());
        Key key = new Key(capability, level.dimension(), neighbour, side.getOpposite());
        BlockCapabilityCache<?, Direction> cache = caches.get(key);
        if (cache == null) {
            if (caches.size() >= MAX_CACHED) caches.clear();
            cache = BlockCapabilityCache.create(capability, server, neighbour, side.getOpposite());
            caches.put(key, cache);
        }
        return (H) cache.getCapability();
    }

    /** Whether {@code source} holds anything {@code filter} lets through. Opens no transaction. */
    public static <T extends Resource> boolean hasAny(ResourceHandler<T> source, Predicate<T> filter) {
        for (int slot = 0, size = source.size(); slot < size; slot++) {
            T resource = source.getResource(slot);
            if (!resource.isEmpty() && source.getAmountAsLong(slot) > 0 && filter.test(resource)) return true;
        }
        return false;
    }

    /**
     * Whether {@code dest} certainly can't take any more: every slot holds something and is at its
     * capacity. Stricter than {@code ResourceHandlerUtil.isFull}, which counts a slot reporting no
     * capacity for "empty" as full; some handlers do that, and would then never be tried.
     */
    public static <T extends Resource> boolean definitelyFull(ResourceHandler<T> dest) {
        int size = dest.size();
        if (size == 0) return false; // unknown: let the transfer decide
        for (int slot = 0; slot < size; slot++) {
            T resource = dest.getResource(slot);
            if (resource.isEmpty() || dest.getAmountAsLong(slot) < dest.getCapacityAsLong(slot, resource)) {
                return false;
            }
        }
        return true;
    }

    /** Whether {@code dest} certainly can't take any more energy. */
    public static boolean definitelyFull(EnergyHandler dest) {
        long capacity = dest.getCapacityAsLong();
        return capacity > 0 && dest.getAmountAsLong() >= capacity;
    }
}
