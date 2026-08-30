package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import org.jetbrains.annotations.Nullable;

/**
 * Block-entity renderer for the Absorption Hopper.
 *
 * <p>The static block model (body + optional connection pipes) is provided by the multipart
 * blockstate JSON.  This renderer's only job is to draw a cyan wireframe box showing the
 * hopper's current pickup area — a 7×7×7-block zone centred at the offset from the
 * block's position.
 *
 * <p>The wireframe is always shown; it is naturally culled by the render bounding box when
 * the block is outside the camera frustum.
 */
public class AbsorptionHopperBlockEntityRenderer
        implements BlockEntityRenderer<AbsorptionHopperBlockEntity, AbsorptionHopperBlockEntityRenderer.HopperState> {

    /** Half-side of the pickup cube — mirrors {@code AbsorptionHopperBlockEntity.PICKUP_RADIUS}. */
    private static final double PICKUP_RADIUS = 3.5;

    /** Wireframe color: cyan, 80 % opaque. */
    private static final int WF_R = 0, WF_G = 205, WF_B = 230, WF_A = 200;
    private static final float WF_LINE_WIDTH = 2.0f;

    @SuppressWarnings("unused")
    public AbsorptionHopperBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    // ── Render state ──────────────────────────────────────────────────────────

    public static class HopperState extends BlockEntityRenderState {
        /**
         * Pickup AABB in block-local coordinates (block origin = [0,0,0]).
         * Null when the wireframe is disabled.
         */
        @Nullable AABB localPickupAABB;
    }

    @Override
    public HopperState createRenderState() {
        return new HopperState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            AbsorptionHopperBlockEntity be,
            HopperState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {

        BlockEntityRenderState.extractBase(be, state, crumbling);

        if (!be.showArea) {
            state.localPickupAABB = null;
            return;
        }

        // Pickup box centre in block-local space (block corner = 0,0,0).
        double cx = 0.5 + be.getOffsetX();
        double cy = 0.5 + be.getOffsetY();
        double cz = 0.5 + be.getOffsetZ();

        state.localPickupAABB = new AABB(
                cx - PICKUP_RADIUS, cy - PICKUP_RADIUS, cz - PICKUP_RADIUS,
                cx + PICKUP_RADIUS, cy + PICKUP_RADIUS, cz + PICKUP_RADIUS);
    }

    @Override
    public void submit(
            HopperState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {

        if (state.localPickupAABB == null) return;

        AABB box = state.localPickupAABB;
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.LINES,
                (pose, vc) -> drawLineBox(vc, pose.pose(),
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ,
                        WF_R, WF_G, WF_B, WF_A, WF_LINE_WIDTH));
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    /**
     * Disables frustum culling for this renderer so the wireframe isn't clipped when
     * the pickup zone (up to 11.5 blocks from the block position) extends outside the
     * block's own small render bounds. Cross-platform equivalent of NeoForge's
     * {@code IBlockEntityRendererExtension#getRenderBoundingBox}, which isn't available
     * on vanilla/Fabric.
     */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
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
