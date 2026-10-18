package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.mobhead.SkullStackDrawer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a String of Skull Lights from its origin hook: a black cord sagging to the far hook (or,
 * while it's being hung, to the hand of whoever is carrying it), and half-size skulls hanging
 * from it, facing out from the walls it hangs on. Lit skulls are drawn full bright and tinted
 * towards the string's colour, as if lit from inside; unlit ones take the world's light.
 */
public class SkullLightsRenderer implements BlockEntityRenderer<SkullLightHookBlockEntity, SkullLightsRenderer.State> {

    private static final float SKULL_SCALE = 0.5f;
    private static final float CORD_WIDTH = 1f / 24f;
    private static final int CORD_COLOR = 0xFF1C1C1C;
    /** How far a lit skull's tint goes from white towards the dye's colour (1 = all the way). */
    private static final float TINT_STRENGTH = 0.65f;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final RenderType CORD = RenderTypes.entitySolid(Identifier.withDefaultNamespace("textures/misc/white.png"));

    public static class State extends BlockEntityRenderState {
        /** The cord's points, relative to the hook's block. Empty when this hook draws nothing. */
        Vec3[] cord = new Vec3[0];
        final List<Skull> skulls = new ArrayList<>();
        int cordLight;
    }

    record Skull(ItemStack stack, Vec3 centre, float yaw, int light, int tint) {}

    private final SkullStackDrawer skulls;

    public SkullLightsRenderer(BlockEntityRendererProvider.Context ctx) {
        this.skulls = new SkullStackDrawer(ctx.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SkullLightHookBlockEntity hook, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(hook, state, crumbling);
        state.cord = new Vec3[0];
        state.skulls.clear();
        Level level = hook.getLevel();
        SkullLights lights = hook.lights();
        if (level == null || lights == null) return;
        Vec3 to = otherEnd(hook, partialTick);
        if (to == null) return;

        Vec3 origin = Vec3.atLowerCornerOf(hook.getBlockPos());
        Vec3 from = hook.attachPoint();
        Vec3 away = awayFromWalls(hook, level);
        Vec3[] points = SkullLightsCurve.points(from, to);
        for (int i = 0; i < points.length; i++) points[i] = points[i].subtract(origin);
        state.cord = points;
        state.cordLight = LevelRenderer.getLightCoords(level, BlockPos.containing(from.lerp(to, 0.5)));

        int tint = lights.lit() ? ARGB.srgbLerp(TINT_STRENGTH, 0xFFFFFFFF, ARGB.opaque(lights.color().getTextureDiffuseColor())) : -1;
        for (SkullLightsCurve.Hang hang : SkullLightsCurve.hangs(from, to)) {
            float yaw = facing(hang.tangent(), away);
            int light = lights.lit() ? FULL_BRIGHT : LevelRenderer.getLightCoords(level, BlockPos.containing(hang.skullCentre()));
            state.skulls.add(new Skull(lights.skullAt(hang.index()), hang.point().subtract(origin), yaw, light, tint));
        }
    }

    /**
     * Which way is out from the walls the string hangs on: the way its hooks stick out, added up
     * and made level. Zero when there's no such way: hooks on facing walls, or on a floor.
     */
    private static Vec3 awayFromWalls(SkullLightHookBlockEntity hook, Level level) {
        Vec3 away = Vec3.atLowerCornerOf(hook.getBlockState().getValue(SkullLightHookBlock.FACING).getUnitVec3i());
        BlockPos endPos = hook.end();
        if (endPos != null) {
            BlockState endState = level.getBlockState(endPos);
            if (endState.is(Registration.SKULL_LIGHT_HOOK.get())) {
                away = away.add(Vec3.atLowerCornerOf(endState.getValue(SkullLightHookBlock.FACING).getUnitVec3i()));
            }
        }
        return new Vec3(away.x, 0, away.z);
    }

    /**
     * The yaw, as a skull block's rotation in degrees, for a skull on a cord running along
     * {@code tangent}: facing square off the cord, on the side away from the walls if there is one.
     * A skull at rotation {@code d} faces ({@code sin d}, {@code -cos d}).
     */
    private static float facing(Vec3 tangent, Vec3 away) {
        float yaw = (float) Math.toDegrees(Math.atan2(tangent.x, tangent.z)) + 90f;
        double rad = Math.toRadians(yaw);
        if (Math.sin(rad) * away.x - Math.cos(rad) * away.z < -1e-3) yaw += 180f;
        return yaw;
    }

    /** Where the string's far end is: its other hook, or the hand of whoever is hanging it. Null if neither. */
    @Nullable
    private static Vec3 otherEnd(SkullLightHookBlockEntity hook, float partialTick) {
        Level level = hook.getLevel();
        if (level == null) return null;
        if (hook.holder() != null) {
            Player holder = level.getPlayerByUUID(hook.holder());
            return holder == null ? null : holder.getRopeHoldPosition(partialTick);
        }
        BlockPos endPos = hook.end();
        if (endPos == null) return null;
        BlockState endState = level.getBlockState(endPos);
        return endState.is(Registration.SKULL_LIGHT_HOOK.get()) ? SkullLightHookBlockEntity.attachPoint(endPos, endState) : null;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.cord.length < 2) return;
        submitCord(state, poseStack, collector);
        for (Skull skull : state.skulls) {
            poseStack.pushPose();
            // Hang from the cord: the skull's top touches it.
            poseStack.translate(skull.centre().x, skull.centre().y - 2 * SkullLightsCurve.SKULL_DROP, skull.centre().z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-skull.yaw()));
            poseStack.scale(SKULL_SCALE, SKULL_SCALE, SKULL_SCALE);
            skulls.submit(skull.stack(), poseStack, collector, skull.light(), skull.tint());
            poseStack.popPose();
        }
    }

    /** The cord: a thin square tube along its points. */
    private static void submitCord(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        Vec3[] points = state.cord;
        int light = state.cordLight;
        collector.submitCustomGeometry(poseStack, CORD, (pose, vc) -> {
            for (int i = 1; i < points.length; i++) {
                Vec3 a = points[i - 1], b = points[i];
                Vec3 along = b.subtract(a).normalize();
                Vec3 side = along.cross(new Vec3(0, 1, 0));
                side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
                Vec3 up = side.cross(along).normalize();
                Vec3 s = side.scale(CORD_WIDTH / 2), u = up.scale(CORD_WIDTH / 2);
                face(vc, pose, a, b, u, s, light);
                face(vc, pose, a, b, s, u.reverse(), light);
                face(vc, pose, a, b, u.reverse(), s.reverse(), light);
                face(vc, pose, a, b, s.reverse(), u, light);
            }
        });
    }

    /** One long face of a cord segment from a to b: offset {@code out} from the centre line, {@code across} wide each way. */
    private static void face(VertexConsumer vc, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 out, Vec3 across, int light) {
        Vec3 n = out.normalize();
        vertex(vc, pose, a.add(out).subtract(across), n, light);
        vertex(vc, pose, b.add(out).subtract(across), n, light);
        vertex(vc, pose, b.add(out).add(across), n, light);
        vertex(vc, pose, a.add(out).add(across), n, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, Vec3 p, Vec3 n, int light) {
        vc.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(CORD_COLOR).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, (float) n.x, (float) n.y, (float) n.z);
    }

    /** The string reaches well outside the hook's block. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /**
     * The box the whole string fits in: both ends, the sag and the skulls under it. NeoForge
     * culls a block entity's renderer by this box (its loader renderer overrides
     * {@code getRenderBoundingBox} with it), so the string still draws when its origin hook is
     * off screen.
     */
    public static AABB renderBounds(SkullLightHookBlockEntity hook) {
        AABB own = new AABB(hook.getBlockPos());
        Vec3 to = hook.lights() == null ? null : otherEnd(hook, 1f);
        if (to == null) return own;
        Vec3 from = hook.attachPoint();
        double drop = SkullLightsCurve.point(from, to, 0.5).y - from.lerp(to, 0.5).y; // negative: the sag
        return own.minmax(new AABB(from, to)).expandTowards(0, drop - 4 * SkullLightsCurve.SKULL_DROP, 0).inflate(0.5);
    }
}
