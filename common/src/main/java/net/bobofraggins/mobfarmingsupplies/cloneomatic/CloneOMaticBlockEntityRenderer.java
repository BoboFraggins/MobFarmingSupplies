package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block-entity renderer for the Clone-O-Matic.
 *
 * <p>Renders a cycling mob display using the same cage-and-orbit presentation as
 * vanilla spawners ({@link SpawnerRenderer#renderEntityInSpawner}).  The displayed mob
 * cycles through all non-misc entity types every {@value #SWITCH_TICKS} game ticks.
 *
 * <p>When DNA sample items are implemented, the entity list will be sourced from the
 * DNA slots instead of the full registry.
 *
 * <p>Rendered in two phases ({@link StateBlockEntityRenderer}):
 * <ul>
 *   <li>{@link #extractRenderState} picks the next entity type and creates (or reuses) a
 *       dummy entity.</li>
 *   <li>{@link #submit} draws it the way a spawner draws its mob.</li>
 * </ul>
 */
public class CloneOMaticBlockEntityRenderer
        extends StateBlockEntityRenderer<CloneOMaticBlockEntity, CloneOMaticBlockEntityRenderer.CloneState> {

    /** Ticks between entity-type switches in the BER display. */
    private static final int SWITCH_TICKS = 5;

    /** Degrees of Y-rotation accumulated per game tick. */
    private static final float DEG_PER_TICK = 360f / (10f * 20f); // one rev per 10 s

    /** Largest scale the displayed mob is drawn at. */
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

    // ── Constructor ──────────────────────────────────────────────────────────────

    public CloneOMaticBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.entityRenderDispatcher = ctx.getEntityRenderer();
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
        @Nullable Entity displayEntity;
        float partialTick;
        float spin;
        float scale = DISPLAY_SCALE;
    }

    @Override
    public CloneState createRenderState() {
        return new CloneState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────────

    @Override
    public void extractRenderState(CloneOMaticBlockEntity be, CloneState state, float partialTick) {
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

        // Retrieve or create a dummy entity (never added to the world).
        Entity dummy = dummyEntityCache.computeIfAbsent(type, t -> {
            try {
                return t.create(level);
            } catch (Exception ignored) {
                return null;
            }
        });

        if (dummy == null) {
            state.displayEntity = null;
            return;
        }

        // Scale down to fit within 0.9 blocks; large mobs (Ghasts, Withers, etc.) would
        // otherwise overflow the block at the fixed 0.5 scale.
        float maxDim = Math.max(dummy.getBbWidth(), dummy.getBbHeight());
        state.scale = Math.min(DISPLAY_SCALE, 0.45f / maxDim);

        state.displayEntity = dummy;
        state.partialTick = partialTick;
    }

    @Override
    public void submit(CloneState state, PoseStack poseStack, MultiBufferSource buffers) {
        if (state.displayEntity == null) return;

        // As a monster spawner draws its mob, but at this renderer's own spin and scale.
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.4f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
        poseStack.translate(0.0f, -0.2f, 0.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0f));
        poseStack.scale(state.scale, state.scale, state.scale);
        try {
            entityRenderDispatcher.render(state.displayEntity, 0.0, 0.0, 0.0, 0.0f, state.partialTick,
                    poseStack, buffers, state.lightCoords);
        } catch (Exception ignored) {
            // some modded mobs can't be drawn outside the world
        }
        poseStack.popPose();
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
