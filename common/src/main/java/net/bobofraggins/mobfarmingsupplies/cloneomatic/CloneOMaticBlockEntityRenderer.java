package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Block-entity renderer for the Clone-O-Matic.
 *
 * <p>Renders a cycling mob display using the same cage-and-orbit presentation as
 * vanilla spawners ({@link SpawnerRenderer#submitEntityInSpawner}).  The displayed mob
 * cycles through all non-misc entity types every {@value #SWITCH_TICKS} game ticks.
 *
 * <p>When DNA sample items are implemented, the entity list will be sourced from the
 * DNA slots instead of the full registry.
 *
 * <p>Implements the two-phase {@link BlockEntityRenderer} API:
 * <ul>
 *   <li>{@link #extractRenderState} — main thread; picks the next entity type,
 *       creates (or reuses) a dummy entity, and extracts its {@link EntityRenderState}.</li>
 *   <li>{@link #submit} — render thread; delegates to
 *       {@link SpawnerRenderer#submitEntityInSpawner}.</li>
 * </ul>
 */
public class CloneOMaticBlockEntityRenderer
        implements BlockEntityRenderer<CloneOMaticBlockEntity, CloneOMaticBlockEntityRenderer.CloneState> {

    /** Ticks between entity-type switches in the BER display. */
    private static final int SWITCH_TICKS = 5;

    /** Degrees of Y-rotation accumulated per game tick. */
    private static final float DEG_PER_TICK = 360f / (10f * 20f); // one rev per 10 s

    /** Entity display scale passed to {@link SpawnerRenderer#submitEntityInSpawner}. */
    private static final float DISPLAY_SCALE = 0.15f;

    /** Particles emitted per game tick when powered. */
    private static final int PARTICLES_PER_TICK = 4;

    // ── Renderer state (per renderer instance, main thread only) ─────────────────

    private final EntityRenderDispatcher entityRenderDispatcher;

    /** Game-time of the last particle spawn; prevents spawning more than once per tick. */
    private long lastParticleGameTime = -1L;

    /**
     * Lazily built; populated once from {@link BuiltInRegistries#ENTITY_TYPE} on first use,
     * filtering out {@link MobCategory#MISC} entries that have no meaningful visual.
     */
    @Nullable
    private List<EntityType<?>> mobTypeList;

    /**
     * Dummy entity instances, one per {@link EntityType}, created on demand and
     * reused across frames to avoid repeated allocation.
     */
    private final Map<EntityType<?>, Entity> dummyEntityCache = new HashMap<>();

    /**
     * Entity types that failed to construct a dummy (rejected by
     * {@link EntityType#canSpawn}, e.g. hostile mobs on Peaceful, or a modded entity
     * class that threw during construction). Remembered permanently so a broken type
     * doesn't retry construction (and potentially re-throw) on every render frame for
     * as long as it stays selected in the rotation — {@link Map#computeIfAbsent} does
     * not cache {@code null} results, so without this a bad type would otherwise be
     * retried 60 times a second, tanking framerate.
     */
    private final Set<EntityType<?>> unspawnableTypes = new HashSet<>();

    /** Synthetic IDs handed to dummy entities — see the {@code setId} call below. */
    private int nextDummyEntityId = 1;

    // ── Constructor ──────────────────────────────────────────────────────────────

    public CloneOMaticBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.entityRenderDispatcher = ctx.entityRenderer();
    }

    // ── Render state ─────────────────────────────────────────────────────────────

    /**
     * Per-frame render state for the Clone-O-Matic BER.
     *
     * <p>{@code displayEntity} is populated during {@link #extractRenderState} and
     * consumed during {@link #submit}.  It may be {@code null} if no renderable entity
     * type is available.
     */
    public static class CloneState extends BlockEntityRenderState {
        @Nullable EntityRenderState displayEntity;
        float spin;
        float scale = DISPLAY_SCALE;
    }

    @Override
    public CloneState createRenderState() {
        return new CloneState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            CloneOMaticBlockEntity be,
            CloneState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {

        BlockEntityRenderState.extractBase(be, state, crumbling);

        Level level = be.getLevel();
        if (level == null) {
            state.displayEntity = null;
            return;
        }

        long gameTime = level.getGameTime();
        boolean powered = be.getBlockState().getValue(CloneOMaticBlock.POWERED);

        // Spin accumulates continuously (degrees).
        state.spin  = (gameTime + partialTick) * DEG_PER_TICK;
        state.scale = DISPLAY_SCALE;

        // ── Spawn portal particles when powered ───────────────────────────────
        // Rate-limited to one burst per game tick regardless of render frame rate.
        if (powered && level instanceof ClientLevel clientLevel && gameTime != lastParticleGameTime) {
            lastParticleGameTime = gameTime;
            BlockPos pos  = be.getBlockPos();
            double   bx   = pos.getX() + 0.5;
            double   by   = pos.getY() + 0.5;
            double   bz   = pos.getZ() + 0.5;
            var      rand = clientLevel.getRandom();
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                double ox = (rand.nextDouble() - 0.5) * 1.6;
                double oy =  rand.nextDouble() * 1.5;
                double oz = (rand.nextDouble() - 0.5) * 1.6;
                clientLevel.addParticle(ParticleTypes.PORTAL,
                        bx + ox, by + oy, bz + oz,
                        ox * 0.05, 0.05, oz * 0.05);
            }
        }

        // Pick entity type — switch every SWITCH_TICKS game ticks.
        List<EntityType<?>> types = getOrBuildMobTypeList();
        if (types.isEmpty()) {
            state.displayEntity = null;
            return;
        }

        int typeIdx = (int) ((gameTime / SWITCH_TICKS) % types.size());
        EntityType<?> type = types.get(typeIdx);

        if (unspawnableTypes.contains(type)) {
            state.displayEntity = null;
            return;
        }

        // Retrieve or create a dummy entity (never added to the world).
        Entity dummy = dummyEntityCache.get(type);
        if (dummy == null) {
            try {
                dummy = type.create(level, EntitySpawnReason.SPAWNER);
            } catch (Throwable ignored) {
                dummy = null;
            }
            if (dummy == null) {
                unspawnableTypes.add(type);
                state.displayEntity = null;
                return;
            }
            // EntityType#create() never assigns an entity ID (only Level#addFreshEntity
            // does that, which we deliberately never call for a decorative dummy). As of
            // MC 26.2, Entity#getId() throws IllegalStateException on the unassigned (0)
            // default, and LivingEntityRenderer's item-model resolution now calls getId()
            // unconditionally — so every dummy failed here. The id value itself is never
            // looked up anywhere (this entity is never registered in a level), so any
            // nonzero value is fine.
            dummy.setId(nextDummyEntityId++);
            dummyEntityCache.put(type, dummy);
        }

        // Scale down to fit within 0.9 blocks; large mobs (Ghasts, Withers, etc.) would
        // otherwise overflow the block at the fixed 0.5 scale.
        float maxDim = Math.max(dummy.getBbWidth(), dummy.getBbHeight());
        state.scale = Math.min(DISPLAY_SCALE, 0.45f / maxDim);

        // Extract the entity's render state (creates a new EntityRenderState).
        try {
            state.displayEntity = entityRenderDispatcher.extractEntity(dummy, partialTick);
        } catch (Exception ignored) {
            // extractEntity() builds a full CrashReport internally before rethrowing on
            // any failure, which is expensive — remember the failure so we never retry
            // this type (matches the unspawnableTypes handling above); without this, a
            // type that always fails here would pay that cost on every frame for as long
            // as it stays selected in the rotation.
            unspawnableTypes.add(type);
            state.displayEntity = null;
        }
    }

    @Override
    public void submit(
            CloneState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {

        if (state.displayEntity == null) return;

        SpawnerRenderer.submitEntityInSpawner(
                poseStack,
                collector,
                state.displayEntity,
                entityRenderDispatcher,
                state.spin,
                state.scale,
                cameraState);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /**
     * Returns (lazily building on first call) the list of all non-misc entity types
     * registered in {@link BuiltInRegistries#ENTITY_TYPE}.
     */
    private List<EntityType<?>> getOrBuildMobTypeList() {
        if (mobTypeList == null) {
            List<EntityType<?>> list = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (type.getCategory() != MobCategory.MISC) {
                    list.add(type);
                }
            }
            mobTypeList = list;
        }
        return mobTypeList;
    }
}
