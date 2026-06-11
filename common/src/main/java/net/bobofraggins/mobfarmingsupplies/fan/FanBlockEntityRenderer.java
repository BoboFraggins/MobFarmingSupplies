package net.bobofraggins.mobfarmingsupplies.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Block-entity renderer for the Fan.
 *
 * <h3>Turbine</h3>
 * <p>Renders the spinning turbine (shaft + blades) as an overlay on top of the static case model.
 * The turbine model ({@code block/fan_turbine}) is loaded as an extra block model via
 * {@link ExtraBlockModels} and looked up by id at render time.
 * Rotation is around the fan's longitudinal axis (Z in model-space = the facing direction in
 * world-space).  One full revolution takes {@value #SECS_PER_REV} seconds.
 * The turbine only spins when {@code POWERED=true}; otherwise it stays stationary at angle 0.
 *
 * <h3>Push-AABB wireframe</h3>
 * <p>When Show Area is enabled an orange wireframe box is drawn showing
 * the current push zone,
 * computed from the upgrade counts synced to the client via the block-entity's NBT.
 * Depth is the maximum reachable range (1 + distance upgrades), not reduced by blocking blocks,
 * so the wireframe shows the <em>configured</em> reach rather than the live effective depth.
 */
public class FanBlockEntityRenderer
        implements BlockEntityRenderer<FanBlockEntity, FanBlockEntityRenderer.FanState> {

    /** Id of the spinning turbine (shaft + blades) overlay model. */
    public static final Identifier TURBINE_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/fan_turbine");

    /** Seconds per full revolution when the fan is powered. */
    private static final float SECS_PER_REV = 2f;
    /** Degrees advanced per game tick (20 ticks/second). */
    private static final float DEG_PER_TICK = 360f / (SECS_PER_REV * 20f);

    /** Maximum upgrades recognised per slot (mirrors FanBlockEntity logic). */
    private static final int MAX_UPGRADES = FanBlockEntity.MAX_UPGRADES;

    /** Wireframe color: orange-gold, 80 % opaque. */
    private static final int   WF_R = 255, WF_G = 160, WF_B = 0, WF_A = 200;
    private static final float WF_LINE_WIDTH = 2.0f;

    // Scratch quaternions reused every frame (submit runs single-threaded).
    private static final Quaternionf FACING_QUAT = new Quaternionf();
    private static final Quaternionf SPIN_QUAT   = new Quaternionf();

    @SuppressWarnings("unused")
    public FanBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    // ── Render state ──────────────────────────────────────────────────────────

    public static class FanState extends BlockEntityRenderState {
        /** Current turbine spin angle in degrees; 0 when unpowered. */
        float spinAngleDeg;
        /** Facing direction of the block — determines spin axis in world space. */
        Direction facing = Direction.NORTH;
        /**
         * Push-AABB in block-local coordinates (block origin = [0,0,0]).
         * Null when the fan is unpowered.
         */
        @Nullable AABB localPushAABB;
    }

    @Override
    public FanState createRenderState() {
        return new FanState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            FanBlockEntity be,
            FanState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {

        BlockEntityRenderState.extractBase(be, state, crumbling);

        Level level = be.getLevel();
        long gameTime = (level != null) ? level.getGameTime() : 0L;

        boolean powered = be.getBlockState().getValue(FanBlock.POWERED);
        state.facing = be.getBlockState().getValue(FanBlock.FACING);
        state.spinAngleDeg = powered ? (gameTime + partialTick) * DEG_PER_TICK : 0f;

        if (be.showArea) {
            // Upgrade counts (client has these via the block-entity's synced NBT).
            int widthCount  = Math.min(be.upgrades.getItem(0).getCount(), MAX_UPGRADES);
            int heightCount = Math.min(be.upgrades.getItem(1).getCount(), MAX_UPGRADES);
            int distanceCount  = Math.min(be.upgrades.getItem(2).getCount(), MAX_UPGRADES);
            int maxDepth    = 3 + distanceCount;

            // World-space AABB, then shift to block-local coords.
            BlockPos pos = be.getBlockPos();
            AABB world = buildAABB(pos, state.facing, maxDepth, widthCount, heightCount);
            state.localPushAABB = world.move(-pos.getX(), -pos.getY(), -pos.getZ());
        } else {
            state.localPushAABB = null;
        }
    }

    @Override
    public void submit(
            FanState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {

        // ── Turbine ───────────────────────────────────────────────────────────
        BlockStateModelPart turbine = ExtraBlockModels.get(TURBINE_MODEL_ID);
        if (turbine != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            switch (state.facing) {
                case UP    -> poseStack.mulPose(FACING_QUAT.rotationX( (float)(Math.PI / 2)));
                case DOWN  -> poseStack.mulPose(FACING_QUAT.rotationX(-(float)(Math.PI / 2)));
                case EAST  -> poseStack.mulPose(FACING_QUAT.rotationY(-(float)(Math.PI / 2)));
                case SOUTH -> poseStack.mulPose(FACING_QUAT.rotationY( (float)  Math.PI));
                case WEST  -> poseStack.mulPose(FACING_QUAT.rotationY( (float)(Math.PI / 2)));
                default    -> {} // NORTH: no rotation
            }
            poseStack.mulPose(SPIN_QUAT.rotationZ(state.spinAngleDeg * Mth.DEG_TO_RAD));
            poseStack.translate(-0.5, -0.5, -0.5);

            collector.submitBlockModel(
                    poseStack,
                    Sheets.cutoutBlockSheet(),
                    List.of(turbine),
                    new int[0],
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            poseStack.popPose();
        }

        // ── Push-AABB wireframe ───────────────────────────────────────────────
        if (state.localPushAABB != null) {
            AABB box = state.localPushAABB;
            collector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.LINES,
                    (pose, vc) -> drawLineBox(vc, pose.pose(),
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            WF_R, WF_G, WF_B, WF_A, WF_LINE_WIDTH));
        }
    }

    // ── Static helpers — AABB building ────────────────────────────────────────

    /**
     * Builds the push AABB in world space.  Mirrors {@code FanBlockEntity.buildAABB}
     * which is private — kept here as a client-side duplicate so the BER does not
     * need access to the server-side entity logic.
     */
    private static AABB buildAABB(BlockPos pos, Direction facing, int depth,
                                   int widthCount, int heightCount) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        double halfH = 0.5 + heightCount;
        double halfW = 0.5 + widthCount;

        int sx = facing.getStepX();
        int sy = facing.getStepY();
        int sz = facing.getStepZ();

        double x0, x1, y0, y1, z0, z1;

        if (sy != 0) {
            double near = cy + sy * 0.5;
            double far  = cy + sy * (0.5 + depth);
            y0 = Math.min(near, far); y1 = Math.max(near, far);
            x0 = cx - halfW; x1 = cx + halfW;
            z0 = cz - halfW; z1 = cz + halfW;
        } else if (sx != 0) {
            double near = cx + sx * 0.5;
            double far  = cx + sx * (0.5 + depth);
            x0 = Math.min(near, far); x1 = Math.max(near, far);
            z0 = cz - halfW; z1 = cz + halfW;
            y0 = cy - halfH; y1 = cy + halfH;
        } else {
            double near = cz + sz * 0.5;
            double far  = cz + sz * (0.5 + depth);
            z0 = Math.min(near, far); z1 = Math.max(near, far);
            x0 = cx - halfW; x1 = cx + halfW;
            y0 = cy - halfH; y1 = cy + halfH;
        }

        return new AABB(x0, y0, z0, x1, y1, z1);
    }

    // ── Static helpers — line drawing ─────────────────────────────────────────

    /**
     * Draws the 12 edges of an axis-aligned bounding box using the LINES render type.
     * Coordinates are in the space already set up by the surrounding {@link PoseStack}.
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
