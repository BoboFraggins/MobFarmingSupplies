package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * A 1.21.1 block capability seen through this package's handler types: lookups are wrapped with
 * {@link Wrappers}, and handlers registered are offered to other mods through {@link Adapters}.
 *
 * @param <H> our handler type, e.g. {@code ResourceHandler<ItemResource>}
 */
public final class BlockCapability<H, C> {

    private final net.neoforged.neoforge.capabilities.BlockCapability<Object, C> raw;
    private final Function<Object, H> wrap;
    private final Function<H, Object> unwrap;

    @SuppressWarnings("unchecked")
    public <R> BlockCapability(net.neoforged.neoforge.capabilities.BlockCapability<R, C> raw,
                               Function<R, H> wrap, Function<H, R> unwrap) {
        this.raw = (net.neoforged.neoforge.capabilities.BlockCapability<Object, C>) raw;
        this.wrap = (Function<Object, H>) wrap;
        this.unwrap = (Function<H, Object>) unwrap;
    }

    /** The NeoForge capability this stands for. */
    public net.neoforged.neoforge.capabilities.BlockCapability<?, C> raw() {
        return raw;
    }

    @Nullable
    public H getCapability(Level level, BlockPos pos, @Nullable C context) {
        Object handler = level.getCapability(raw, pos, context);
        return handler == null ? null : wrap.apply(handler);
    }

    /** A cached lookup of this capability, as {@link BlockCapabilityCache}. */
    public Cache<H> createCache(ServerLevel level, BlockPos pos, @Nullable C context) {
        return new Cache<>(BlockCapabilityCache.create(raw, level, pos, context), wrap);
    }

    /** Offers {@code provider}'s handlers for {@code type} to everything that looks this capability up. */
    public <BE extends BlockEntity> void registerBlockEntity(RegisterCapabilitiesEvent event,
                                                             BlockEntityType<BE> type,
                                                             ICapabilityProvider<? super BE, C, H> provider) {
        event.registerBlockEntity(raw, type, (be, context) -> {
            H handler = provider.getCapability(be, context);
            return handler == null ? null : unwrap.apply(handler);
        });
    }

    /** A {@link BlockCapabilityCache} whose handler is wrapped once per handler the cache returns. */
    public static final class Cache<H> {
        private final BlockCapabilityCache<Object, ?> cache;
        private final Function<Object, H> wrap;
        @Nullable private Object lastRaw;
        @Nullable private H lastWrapped;

        private Cache(BlockCapabilityCache<Object, ?> cache, Function<Object, H> wrap) {
            this.cache = cache;
            this.wrap = wrap;
        }

        @Nullable
        public H getCapability() {
            Object handler = cache.getCapability();
            if (handler == null) return null;
            if (handler != lastRaw) {
                lastRaw = handler;
                lastWrapped = wrap.apply(handler);
            }
            return lastWrapped;
        }
    }
}
