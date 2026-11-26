package net.bobofraggins.mobfarmingsupplies.forge.transfer.api;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block capability seen through this package's handler types: lookups are wrapped with
 * {@link Wrappers}, and handlers registered are offered to other mods through {@link Adapters}.
 *
 * <p>Forge 1.20.1 has no block capability registry (NeoForge's {@code RegisterCapabilitiesEvent});
 * capabilities hang off block entities. Handlers registered here are attached to block entities
 * with {@link AttachCapabilitiesEvent}: per block entity type, or per block for the blocks
 * registered with {@link #registerBlock} (those are asked with the block entity at that position,
 * so a two-block machine answers at the half with a block entity; our own lookups ask at either half).
 *
 * @param <H> our handler type, e.g. {@code ResourceHandler<ItemResource>}
 */
public final class BlockCapability<H, C> {

    private static final List<BlockCapability<?, ?>> ALL = new ArrayList<>();

    private final Capability<Object> raw;
    private final Function<Object, H> wrap;
    private final Function<H, Object> unwrap;
    private final Map<BlockEntityType<?>, List<ICapabilityProvider2<BlockEntity, C, H>>> byType = new IdentityHashMap<>();
    private final Map<Block, List<IBlockCapabilityProvider<H, C>>> byBlock = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <R> BlockCapability(Capability<R> raw, Function<R, H> wrap, Function<H, R> unwrap) {
        this.raw = (Capability<Object>) (Capability<?>) raw;
        this.wrap = (Function<Object, H>) wrap;
        this.unwrap = (Function<H, Object>) unwrap;
        ALL.add(this);
    }

    /** The Forge capability this stands for. */
    public Capability<?> raw() {
        return raw;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public H getCapability(Level level, BlockPos pos, @Nullable C context) {
        BlockState state = level.getBlockState(pos);
        List<IBlockCapabilityProvider<H, C>> blockProviders = byBlock.get(state.getBlock());
        BlockEntity be = level.getBlockEntity(pos);
        if (blockProviders != null) {
            for (IBlockCapabilityProvider<H, C> provider : blockProviders) {
                H handler = provider.getCapability(level, pos, state, be, context);
                if (handler != null) return handler;
            }
        }
        if (be == null) return null;
        LazyOptional<Object> found = be.getCapability(raw, context instanceof Direction d ? d : null);
        return found.resolve().map(wrap::apply).orElse(null);
    }

    /** A lookup of this capability at one spot, kept for repeated use. */
    public Cache<H> createCache(ServerLevel level, BlockPos pos, @Nullable C context) {
        return new Cache<>(this, level, pos, context);
    }

    /** Offers {@code provider}'s handlers for {@code type} to everything that looks this capability up. */
    @SuppressWarnings("unchecked")
    public <BE extends BlockEntity> void registerBlockEntity(RegisterCapabilitiesEvent event,
                                                             BlockEntityType<BE> type,
                                                             ICapabilityProvider2<? super BE, C, H> provider) {
        byType.computeIfAbsent(type, t -> new ArrayList<>()).add((ICapabilityProvider2<BlockEntity, C, H>) provider);
    }

    /** Offers {@code provider}'s handlers for these blocks, with or without a block entity. */
    public void registerBlock(RegisterCapabilitiesEvent event, IBlockCapabilityProvider<H, C> provider, Block... blocks) {
        for (Block block : blocks) byBlock.computeIfAbsent(block, b -> new ArrayList<>()).add(provider);
    }

    /** Asks the providers registered for {@code be}, for other mods' lookups through the block entity. */
    @Nullable
    @SuppressWarnings("unchecked")
    private Object provide(BlockEntity be, @Nullable Direction side) {
        C context = (C) side;
        List<ICapabilityProvider2<BlockEntity, C, H>> typeProviders = byType.get(be.getType());
        if (typeProviders != null) {
            for (ICapabilityProvider2<BlockEntity, C, H> provider : typeProviders) {
                H handler = provider.getCapability(be, context);
                if (handler != null) return unwrap.apply(handler);
            }
        }
        Level level = be.getLevel();
        if (level != null) {
            List<IBlockCapabilityProvider<H, C>> blockProviders = byBlock.get(be.getBlockState().getBlock());
            if (blockProviders != null) {
                for (IBlockCapabilityProvider<H, C> provider : blockProviders) {
                    H handler = provider.getCapability(level, be.getBlockPos(), be.getBlockState(), be, context);
                    if (handler != null) return unwrap.apply(handler);
                }
            }
        }
        return null;
    }

    private boolean handles(BlockEntity be) {
        return byType.containsKey(be.getType()) || byBlock.containsKey(be.getBlockState().getBlock());
    }

    // ── Attaching ────────────────────────────────────────────────────────────────

    /** Attaches the registered handlers to a new block entity; called for {@code AttachCapabilitiesEvent<BlockEntity>}. */
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();
        Map<Capability<?>, BlockCapability<?, ?>> mine = new HashMap<>();
        for (BlockCapability<?, ?> capability : ALL) {
            if (capability.handles(be)) mine.put(capability.raw, capability);
        }
        if (mine.isEmpty()) return;
        Attached attached = new Attached(be, mine);
        event.addCapability(new net.minecraft.resources.ResourceLocation("mobfarmingsupplies", "handlers"), attached);
        event.addListener(attached::invalidate);
    }

    /**
     * Tells anything holding on to {@code pos}'s handlers to look them up again (NeoForge's
     * {@code level.invalidateCapabilities(pos)}), e.g. after a side changes mode.
     */
    public static void invalidate(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return;
        be.getCapability(Attached.KEY).ifPresent(Attached::invalidate);
    }

    /** One block entity's handlers, made on request and kept until invalidated. */
    private static final class Attached implements ICapabilityProvider {
        /** Lets {@link #invalidate(Level, BlockPos)} find this provider through the block entity. */
        static final Capability<Attached> KEY = net.minecraftforge.common.capabilities.CapabilityManager.get(
                new net.minecraftforge.common.capabilities.CapabilityToken<>() {});

        private final BlockEntity be;
        private final Map<Capability<?>, BlockCapability<?, ?>> capabilities;
        private final Map<Capability<?>, Map<Direction, LazyOptional<?>>> sided = new HashMap<>();
        private final Map<Capability<?>, LazyOptional<?>> unsided = new HashMap<>();
        private final LazyOptional<Attached> self = LazyOptional.of(() -> this);

        Attached(BlockEntity be, Map<Capability<?>, BlockCapability<?, ?>> capabilities) {
            this.be = be;
            this.capabilities = capabilities;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == KEY) return self.cast();
            BlockCapability<?, ?> capability = capabilities.get(cap);
            if (capability == null) return LazyOptional.empty();
            LazyOptional<?> known = side == null ? unsided.get(cap)
                    : sided.computeIfAbsent(cap, c -> new EnumMap<>(Direction.class)).get(side);
            if (known != null && known.isPresent()) return known.cast();
            Object handler = capability.provide(be, side);
            if (handler == null) return LazyOptional.empty();
            LazyOptional<Object> made = LazyOptional.of(() -> handler);
            if (side == null) unsided.put(cap, made);
            else sided.get(cap).put(side, made);
            return made.cast();
        }

        void invalidate() {
            unsided.values().forEach(LazyOptional::invalidate);
            unsided.clear();
            sided.values().forEach(m -> m.values().forEach(LazyOptional::invalidate));
            sided.clear();
        }
    }

    /** A lookup at one spot: looked up again each time, the wrapper kept while the handler is the same one. */
    public static final class Cache<H> {
        private final BlockCapability<H, ?> capability;
        private final ServerLevel level;
        private final BlockPos pos;
        @Nullable private final Object context;

        @SuppressWarnings("unchecked")
        private <C> Cache(BlockCapability<H, C> capability, ServerLevel level, BlockPos pos, @Nullable C context) {
            this.capability = capability;
            this.level = level;
            this.pos = pos;
            this.context = context;
        }

        @Nullable
        @SuppressWarnings({"unchecked", "rawtypes"})
        public H getCapability() {
            if (!level.isLoaded(pos)) return null;
            return (H) ((BlockCapability) capability).getCapability(level, pos, context);
        }
    }

    // ── Provider shapes (NeoForge's interfaces of these names) ─────────────────

    @FunctionalInterface
    public interface ICapabilityProvider2<O, C, H> {
        @Nullable
        H getCapability(O object, @Nullable C context);
    }

    @FunctionalInterface
    public interface IBlockCapabilityProvider<H, C> {
        @Nullable
        H getCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be, @Nullable C context);
    }
}
