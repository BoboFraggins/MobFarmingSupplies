package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Draws the Circuit Inscriber's moving parts over its block model (the base): two three-jointed
 * arms that rest half curled while it's idle and curl fully in over the work surface while it
 * works, two lasers from their tips down to the surface, and the item being worked on, lying on
 * the surface.
 *
 * <p>Model space is in pixels, facing north: the work surface is the top of the base at y 8, the
 * arms stand at the back (z 12.5) at x 3.5 and 12.5, and each segment pivots about the joint
 * below it.
 */
public class CircuitInscriberRenderer
        implements BlockEntityRenderer<CircuitInscriberBlockEntity, CircuitInscriberRenderer.State> {

    private static final Arm RIGHT = new Arm(12.5f, 1, "right");
    private static final Arm LEFT = new Arm(3.5f, -1, "left");

    // Joint heights along each arm, and the tip's far end, where the laser comes out.
    private static final float SHOULDER_Y = 9f;
    private static final float ELBOW_Y = 13.75f;
    private static final float WRIST_Y = 18.25f;
    private static final float TIP_END_Y = 23f;
    private static final float ARM_Z = 12.5f;

    /** How far the arms are curled at rest, between crafts: halfway. 0 would stand them straight up. */
    public static final float REST_CURL = 0.5f;

    // Fully curled, like a desk lamp: each arm turns inward, stays upright at the shoulder, leans its
    // middle 45 degrees forward and hangs its tip almost straight down, about 4 pixels above the item.
    private static final float YAW = 35f;
    private static final float SHOULDER_BEND = 0f;
    private static final float ELBOW_BEND = 45f;
    private static final float WRIST_BEND = 120f;

    /** Where the item lies on the work surface (pixels), and how large it's drawn. */
    private static final float ITEM_X = 8f;
    private static final float ITEM_Z = 8f;
    private static final float ITEM_SCALE = 0.375f;
    /** The item's turn on the surface: a quarter turn counter-clockwise, seen from above. */
    private static final float ITEM_TURN = 90f;

    private static final float SURFACE_Y = 8.02f;
    private static final float LASER_WIDTH = 0.3f;
    /** How far forward of its tip (toward the front) a beam lands, in pixels. */
    private static final float LASER_LEAD = 1f;
    /** How far a beam's spot sweeps either way, across and along the machine, in pixels. */
    private static final float SWEEP_X = 1.08f;
    private static final float SWEEP_Z = 0.96f;
    private static final int LASER_CORE = 0xFFFF5050;
    private static final int LASER_GLOW = 0x80FF1010;

    private final ItemModelResolver itemModelResolver;

    /** One arm: where it stands, which way it turns inward, and its segment models. */
    private record Arm(float x, int inward, String side) {
        Identifier first() {
            return id("block/circuit_inscriber_" + side + "_first");
        }

        Identifier middle() {
            return id("block/circuit_inscriber_" + side + "_middle");
        }

        Identifier tip() {
            return id("block/circuit_inscriber_" + side + "_tip");
        }

        /** The tip with its red light lit, while the machine works. */
        Identifier activeTip() {
            return id("block/circuit_inscriber_" + side + "_tip_active");
        }
    }

    /** The base, which the item renderer draws (placed, it's the block model). */
    public static final Identifier BASE_MODEL_ID = id("block/circuit_inscriber_base");
    /** A 1.2-pixel cube centred on each joint, hiding the gap between segments when they bend. */
    public static final Identifier JOINT_MODEL_ID = id("block/circuit_inscriber_joint");

    public static final List<Identifier> MODEL_IDS = List.of(BASE_MODEL_ID, JOINT_MODEL_ID,
            RIGHT.first(), RIGHT.middle(), RIGHT.tip(), RIGHT.activeTip(),
            LEFT.first(), LEFT.middle(), LEFT.tip(), LEFT.activeTip());

    /** Full brightness, for the lit tips. */
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        /** How far the arms have curled, eased: {@link #REST_CURL} at rest, 1 working over the surface. */
        float curl;
        boolean lasers;
        /** Working: the tips' lights are lit. */
        boolean active;
        float time;
        final ItemStackRenderState item = new ItemStackRenderState();
    }

    public CircuitInscriberRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, path);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CircuitInscriberBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.facing = be.getBlockState().getValue(CircuitInscriberBlock.FACING);
        float progress = Mth.lerp(partialTick, be.prevArmProgress, be.armProgress);
        float eased = progress * progress * (3 - 2 * progress); // ease in and out
        state.curl = REST_CURL + (1 - REST_CURL) * eased;
        state.active = be.isCrafting();
        state.lasers = state.active && progress >= 0.98f;
        state.time = be.clientTicks + partialTick;
        state.item.clear();
        if (!be.displayItem.isEmpty()) {
            itemModelResolver.updateForTopItem(state.item, be.displayItem, ItemDisplayContext.FIXED,
                    be.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180f));
        poseStack.translate(-0.5f, 0f, -0.5f);
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);

        RenderType parts = Sheets.cutoutBlockSheet();
        Vector3f rightTip = submitArm(RIGHT, state.curl, state.active, parts, state.lightCoords,
                OverlayTexture.NO_OVERLAY, poseStack, collector);
        Vector3f leftTip = submitArm(LEFT, state.curl, state.active, parts, state.lightCoords,
                OverlayTexture.NO_OVERLAY, poseStack, collector);

        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(ITEM_X, SURFACE_Y + 0.05f, ITEM_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(ITEM_TURN));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            poseStack.scale(16 * ITEM_SCALE, 16 * ITEM_SCALE, 16 * ITEM_SCALE);
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.lasers) {
            // Each beam slants a little forward of its tip and sweeps across the item, out of step
            // with the other.
            Vector3f rightSpot = new Vector3f(rightTip.x + SWEEP_X * Mth.sin(state.time * 0.31f), SURFACE_Y,
                    rightTip.z - LASER_LEAD + SWEEP_Z * Mth.cos(state.time * 0.23f));
            Vector3f leftSpot = new Vector3f(leftTip.x + SWEEP_X * Mth.sin(state.time * 0.27f + 2f), SURFACE_Y,
                    leftTip.z - LASER_LEAD + SWEEP_Z * Mth.cos(state.time * 0.35f + 1f));
            collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, consumer) -> {
                beam(consumer, pose, rightTip, rightSpot);
                beam(consumer, pose, leftTip, leftSpot);
            });
        }
        poseStack.popPose();
    }

    /**
     * Draws both arms curled by {@code curl} (0 up, 1 working), in a pose that's in pixels: the item
     * renderer draws the inventory icon's arms with this, at {@link #REST_CURL}. The tips are unlit.
     */
    public static void submitArms(float curl, RenderType renderType, int light, int overlay, PoseStack poseStack,
                                  SubmitNodeCollector collector) {
        submitArm(RIGHT, curl, false, renderType, light, overlay, poseStack, collector);
        submitArm(LEFT, curl, false, renderType, light, overlay, poseStack, collector);
    }

    /**
     * Draws an arm curled by {@code c}, its tip lit bright red and glowing if {@code active}; returns
     * where its tip's end is, in model space.
     */
    private static Vector3f submitArm(Arm arm, float c, boolean active, RenderType renderType, int light,
                                      int overlay, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        pivot(poseStack, arm.x(), SHOULDER_Y, Axis.YP.rotationDegrees(arm.inward() * YAW * c));
        pivot(poseStack, arm.x(), SHOULDER_Y, Axis.XP.rotationDegrees(-SHOULDER_BEND * c));
        submitPart(arm.first(), renderType, light, overlay, poseStack, collector);
        submitJoint(arm.x(), SHOULDER_Y, renderType, light, overlay, poseStack, collector);
        pivot(poseStack, arm.x(), ELBOW_Y, Axis.XP.rotationDegrees(-ELBOW_BEND * c));
        submitPart(arm.middle(), renderType, light, overlay, poseStack, collector);
        submitJoint(arm.x(), ELBOW_Y, renderType, light, overlay, poseStack, collector);
        pivot(poseStack, arm.x(), WRIST_Y, Axis.XP.rotationDegrees(-WRIST_BEND * c));
        if (active) submitPart(arm.activeTip(), renderType, FULL_BRIGHT, overlay, poseStack, collector);
        else submitPart(arm.tip(), renderType, light, overlay, poseStack, collector);
        submitJoint(arm.x(), WRIST_Y, renderType, light, overlay, poseStack, collector);

        // The tip's end in model space: through this arm's turns, but not the block's.
        Vector3f end = new Vector3f(arm.x(), TIP_END_Y, ARM_Z);
        PoseStack local = new PoseStack();
        pivot(local, arm.x(), SHOULDER_Y, Axis.YP.rotationDegrees(arm.inward() * YAW * c));
        pivot(local, arm.x(), SHOULDER_Y, Axis.XP.rotationDegrees(-SHOULDER_BEND * c));
        pivot(local, arm.x(), ELBOW_Y, Axis.XP.rotationDegrees(-ELBOW_BEND * c));
        pivot(local, arm.x(), WRIST_Y, Axis.XP.rotationDegrees(-WRIST_BEND * c));
        local.last().pose().transformPosition(end);
        poseStack.popPose();
        return end;
    }

    /** Rotates about the point (x, y, ARM_Z). */
    private static void pivot(PoseStack poseStack, float x, float y, org.joml.Quaternionf rotation) {
        poseStack.translate(x, y, ARM_Z);
        poseStack.mulPose(rotation);
        poseStack.translate(-x, -y, -ARM_Z);
    }

    /** Draws a joint cube centred on (x, y, ARM_Z), turned with the segment that starts there. */
    private static void submitJoint(float x, float y, RenderType renderType, int light, int overlay,
                                    PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(x, y, ARM_Z);
        submitPart(JOINT_MODEL_ID, renderType, light, overlay, poseStack, collector);
        poseStack.popPose();
    }

    /** Draws a segment model; the pose is in pixels and the models in block units. */
    private static void submitPart(Identifier modelId, RenderType renderType, int light, int overlay,
                                   PoseStack poseStack, SubmitNodeCollector collector) {
        BlockStateModelPart part = ExtraBlockModels.get(modelId);
        if (part == null) return;
        poseStack.pushPose();
        poseStack.scale(16f, 16f, 16f);
        collector.submitBlockModel(poseStack, renderType, List.of(part), new int[0], light, overlay, 0);
        poseStack.popPose();
    }

    /** A thin glowing beam from {@code from} to {@code to}: two crossed quads and a spot where it lands. */
    private static void beam(VertexConsumer consumer, PoseStack.Pose pose, Vector3f from, Vector3f to) {
        float w = LASER_WIDTH;
        quad(consumer, pose, from.x - w, from.y, from.z, from.x + w, from.y, from.z,
                to.x + w, to.y, to.z, to.x - w, to.y, to.z, LASER_CORE);
        quad(consumer, pose, from.x, from.y, from.z - w, from.x, from.y, from.z + w,
                to.x, to.y, to.z + w, to.x, to.y, to.z - w, LASER_CORE);
        float s = 0.8f;
        quad(consumer, pose, to.x - s, to.y, to.z - s, to.x - s, to.y, to.z + s,
                to.x + s, to.y, to.z + s, to.x + s, to.y, to.z - s, LASER_GLOW);
    }

    /** A quad seen from both sides. */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4, int colour) {
        consumer.addVertex(pose, x1, y1, z1).setColor(colour);
        consumer.addVertex(pose, x2, y2, z2).setColor(colour);
        consumer.addVertex(pose, x3, y3, z3).setColor(colour);
        consumer.addVertex(pose, x4, y4, z4).setColor(colour);
        consumer.addVertex(pose, x4, y4, z4).setColor(colour);
        consumer.addVertex(pose, x3, y3, z3).setColor(colour);
        consumer.addVertex(pose, x2, y2, z2).setColor(colour);
        consumer.addVertex(pose, x1, y1, z1).setColor(colour);
    }
}
