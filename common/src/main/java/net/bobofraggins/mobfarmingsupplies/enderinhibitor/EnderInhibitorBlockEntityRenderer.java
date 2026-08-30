package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import org.jetbrains.annotations.Nullable;

/**
 * Block-entity renderer for the Ender Inhibitor.
 *
 * <p>Draws a slowly rotating Ender Pearl that hovers just beyond the prong tips,
 * in whatever direction the inhibitor faces.  One full revolution takes
 * {@value #SECS_PER_REV} seconds.
 *
 * <p>Also optionally draws a wireframe cube showing the inhibitor's current
 * teleport-suppression area — a {@code (2*RANGE+1)}-block cube centred on the
 * offset suppression point — when {@link EnderInhibitorBlockEntity#showArea} is set.
 */
public class EnderInhibitorBlockEntityRenderer
        implements BlockEntityRenderer<EnderInhibitorBlockEntity, EnderInhibitorBlockEntityRenderer.PearlState> {

    /** Wireframe color: violet, 80% opaque — matches the inhibitor's portal-particle theme. */
    private static final int WF_R = 165, WF_G = 60, WF_B = 230, WF_A = 200;
    private static final float WF_LINE_WIDTH = 2.0f;

    /** Seconds for one complete rotation. */
    private static final float SECS_PER_REV = 10f;
    /** Degrees advanced per game tick (20 ticks / second). */
    private static final float DEG_PER_TICK = 360f / (SECS_PER_REV * 20f);

    /**
     * Distance from the block centre to the pearl centre, in blocks.
     * The prong tips reach 6/16 = 0.375 blocks from centre; 0.45 nestles the
     * pearl just inside the top face so it sits among the prongs.
     */
    private static final float PEARL_OFFSET = 0.225f;

    // Lazily initialized — ItemStack cannot be created in a static initializer because
    // item data components may not be bound yet when the renderer class is first loaded.
    private ItemStack enderPearlStack;

    // Scratch quaternion reused every frame (submit runs on a single thread).
    private static final Quaternionf SCRATCH_QUAT = new Quaternionf();

    private final ItemModelResolver itemModelResolver;

    public EnderInhibitorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    // ── Render state ──────────────────────────────────────────────────────────────

    public static class PearlState extends BlockEntityRenderState {
        /** Rotation angle in degrees, updated every extract call. */
        float angleDeg;
        /** FACING of the block — determines where the pearl hovers. */
        Direction facing = Direction.UP;
        /**
         * Item render state populated in {@code extractRenderState} and consumed in
         * {@code submit}.  Cleared and re-populated each frame so it always reflects
         * the current item appearance.
         */
        final ItemStackRenderState itemState = new ItemStackRenderState();
        /**
         * Suppression-area AABB in block-local coordinates (block origin = [0,0,0]).
         * Null when the wireframe is disabled.
         */
        @Nullable AABB localAreaAABB;
    }

    @Override
    public PearlState createRenderState() {
        return new PearlState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            EnderInhibitorBlockEntity be,
            PearlState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {

        BlockEntityRenderState.extractBase(be, state, crumbling);

        Level level = be.getLevel();
        long gameTime = (level != null) ? level.getGameTime() : 0L;
        state.angleDeg = (gameTime + partialTick) * DEG_PER_TICK;
        state.facing   = be.getBlockState().getValue(EnderInhibitorBlock.FACING);

        if (be.showArea) {
            double cx = be.getOffsetX();
            double cy = be.getOffsetY();
            double cz = be.getOffsetZ();
            int range = EnderInhibitorEvents.RANGE;
            state.localAreaAABB = new AABB(
                    cx - range, cy - range, cz - range,
                    cx + range + 1, cy + range + 1, cz + range + 1);
        } else {
            state.localAreaAABB = null;
        }

        // Populate item render state (runs on the main thread, safe to call MC APIs).
        if (enderPearlStack == null) enderPearlStack = new ItemStack(Items.ENDER_PEARL);
        state.itemState.clear();
        itemModelResolver.updateForTopItem(
                state.itemState,
                enderPearlStack,
                ItemDisplayContext.FIXED,
                level,
                null, // entity / ItemOwner — not needed for a simple item
                0     // seed
        );
    }

    @Override
    public void submit(
            PearlState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {

        if (state.localAreaAABB != null) {
            AABB box = state.localAreaAABB;
            collector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.LINES,
                    (pose, vc) -> drawLineBox(vc, pose.pose(),
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            WF_R, WF_G, WF_B, WF_A, WF_LINE_WIDTH));
        }

        if (state.itemState.isEmpty()) return;

        Direction facing = state.facing;

        poseStack.pushPose();

        // ── Position ─────────────────────────────────────────────────────────────
        // Translate from the block origin to the pearl centre: block centre (0.5, 0.5, 0.5)
        // plus PEARL_OFFSET in the FACING direction.
        poseStack.translate(
                0.5 + facing.getStepX() * PEARL_OFFSET,
                0.5 + facing.getStepY() * PEARL_OFFSET,
                0.5 + facing.getStepZ() * PEARL_OFFSET);

        // ── Rotation ─────────────────────────────────────────────────────────────
        // Spin slowly around the world Y axis regardless of facing, so the pearl
        // always appears to orbit rather than flip with the block orientation.
        float angleRad = state.angleDeg * Mth.DEG_TO_RAD;
        poseStack.mulPose(SCRATCH_QUAT.rotationY(angleRad));

        // ── Submit ───────────────────────────────────────────────────────────────
        // Scale down to roughly gem-sized; ItemDisplayContext.FIXED renders at
        // full item-frame scale so we need to bring it in ourselves.
        poseStack.scale(0.4f, 0.4f, 0.4f);
        state.itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    // ── Static helpers — line drawing ─────────────────────────────────────────

    /**
     * Draws the 12 edges of an axis-aligned bounding box using the LINES render type.
     */
    static void drawLineBox(VertexConsumer vc, Matrix4f mat,
                             double x0, double y0, double z0,
                             double x1, double y1, double z1,
                             int r, int g, int b, int a, float lineWidth) {
        float fx0 = (float) x0, fy0 = (float) y0, fz0 = (float) z0;
        float fx1 = (float) x1, fy1 = (float) y1, fz1 = (float) z1;
        // Bottom face
        lineV(vc, mat, fx0, fy0, fz0, fx1, fy0, fz0, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy0, fz0, fx1, fy0, fz1, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy0, fz1, fx0, fy0, fz1, r, g, b, a, lineWidth);
        lineV(vc, mat, fx0, fy0, fz1, fx0, fy0, fz0, r, g, b, a, lineWidth);
        // Top face
        lineV(vc, mat, fx0, fy1, fz0, fx1, fy1, fz0, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy1, fz0, fx1, fy1, fz1, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy1, fz1, fx0, fy1, fz1, r, g, b, a, lineWidth);
        lineV(vc, mat, fx0, fy1, fz1, fx0, fy1, fz0, r, g, b, a, lineWidth);
        // Vertical edges
        lineV(vc, mat, fx0, fy0, fz0, fx0, fy1, fz0, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy0, fz0, fx1, fy1, fz0, r, g, b, a, lineWidth);
        lineV(vc, mat, fx1, fy0, fz1, fx1, fy1, fz1, r, g, b, a, lineWidth);
        lineV(vc, mat, fx0, fy0, fz1, fx0, fy1, fz1, r, g, b, a, lineWidth);
    }

    private static void lineV(VertexConsumer vc, Matrix4f mat,
                               float x0, float y0, float z0,
                               float x1, float y1, float z1,
                               int r, int g, int b, int a, float lineWidth) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-5f) return;
        dx /= len; dy /= len; dz /= len;
        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(lineWidth);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(dx, dy, dz).setLineWidth(lineWidth);
    }
}
