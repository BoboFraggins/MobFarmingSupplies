package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Draws the Gravitational Anomaly as a black hole: its shadow, the accretion disk bent by its
 * gravity ({@link AccretionDisk}), and, while it's active, relativistic jets from its poles.
 *
 * <p>The shadow and the disk's images are flat, on the plane through the centre facing the viewer,
 * one just in front of the other: the shadow, the secondary images, then the primary ones. Each
 * image is drawn twice: once blended over what's behind it, for its colour and opacity, then twice
 * more on top as glowing swirls. The swirls turn clockwise seen from above, faster toward the
 * middle; so that shearing never winds them up, two copies take turns, each fading out before it
 * starts again. The jets are drawn in place, as glowing strips turned to face the viewer.
 *
 * <p>Model space is in pixels, centred on the black hole.
 */
public class GravitationalAnomalyRenderer
        implements BlockEntityRenderer<GravitationalAnomalyBlockEntity, GravitationalAnomalyRenderer.State> {

    private static final RenderType SHADOW = RenderTypes.entitySolid(texture("gravitational_anomaly"));
    // Everything else is translucent and glowing, drawn as "eyes" (blended, unlit, not writing
    // depth). Glows are blended toward their colour rather than added, so their patterns are in
    // their textures' alpha.
    private static final Identifier DISK = texture("gravitational_anomaly_disk");
    private static final Identifier SWIRLS = texture("gravitational_anomaly_streaks");
    private static final Identifier JET = texture("gravitational_anomaly_jet");
    private static final Identifier GLOW = texture("gravitational_anomaly_glow");

    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int SHADOW_SEGMENTS = 64;

    /** How far in front of the shadow each image sits, in pixels, so they don't flicker. */
    private static final float SECONDARY_LIFT = 0.02f;
    private static final float PRIMARY_LIFT = 0.04f;
    private static final float SWIRL_LIFT = 0.01f;

    /** How many times the swirl texture repeats round the disk. */
    private static final int SWIRL_REPEATS = 3;
    /** Turns per second: the disk's colour as a whole, and the swirls at its inner edge. */
    private static final float DISK_TURN = 0.12f;
    private static final float INNER_TURN = 0.25f;
    /** Seconds each copy of the swirls lasts before it fades out and starts again. */
    private static final float SWIRL_CYCLE = 4f;

    /** How far each jet reaches from the centre, in pixels: 36 from tip to tip. */
    private static final float JET_LENGTH = 18f;
    /** Where the jets start, just off the centre. */
    private static final float JET_START = 1.5f;
    private static final int JET_SEGMENTS = 16;
    /** How far out the jets take to brighten from nothing, so they rise out of the shadow. */
    private static final float JET_FADE_IN = 5f;
    /** How fast the jet's texture streams outward, in jet lengths per second. */
    private static final float JET_FLOW = 0.9f;
    private static final float[] JET_SHEATH = {0.42f, 0.62f, 1f};
    private static final float[] JET_CORE = {0.92f, 0.96f, 1f};

    public static class State extends BlockEntityRenderState {
        /** From the centre toward the camera, in blocks. */
        final Vector3f toViewer = new Vector3f(0, 0, 1);
        float seconds;
        /** How far out the jets reach, 0 to 1. */
        float jets;
    }

    public GravitationalAnomalyRenderer(BlockEntityRendererProvider.Context ctx) {
        AccretionDisk.warmUp();
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/block/" + name + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GravitationalAnomalyBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        BlockPos pos = be.getBlockPos();
        state.toViewer.set(camera.x - (pos.getX() + 0.5), camera.y - (pos.getY() + 0.5),
                camera.z - (pos.getZ() + 0.5));
        long ticks = be.getLevel() == null ? 0 : be.getLevel().getGameTime() % 72_000L;
        state.seconds = (ticks + partialTick) / 20f;
        state.jets = Mth.lerp(partialTick, be.prevJets, be.jets);
    }

    /** The disk and jets reach well past the block, so don't skip it when the block is off screen. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        submitBlackHole(state.toViewer, state.seconds, state.jets, true, poseStack, collector);
    }

    /**
     * Draws the black hole centred in a block-sized pose, seen from {@code toViewer} (from the
     * centre toward the viewer, in that pose's units); the item renderer uses this too.
     * {@code inWorld}, the translucent parts are drawn in a {@link GlowPass}, after water and
     * clouds, rather than with the block entity, where those would be drawn over them.
     */
    public static void submitBlackHole(Vector3f toViewer, float seconds, float jets, boolean inWorld,
                                       PoseStack poseStack, SubmitNodeCollector collector) {
        Vector3f o = new Vector3f(toViewer);
        if (o.lengthSquared() < 1e-8f) o.set(0, 0, 1);
        o.normalize();
        // Two directions across the viewer's sky, e1 × e2 = o, so quads wound e1 → e2 face the viewer.
        Vector3f e1 = Math.abs(o.y) > 0.99f ? new Vector3f(1, 0, 0).cross(o) : new Vector3f(0, 1, 0).cross(o);
        e1.normalize();
        Vector3f e2 = new Vector3f(o).cross(e1);

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);

        collector.order(0).submitCustomGeometry(poseStack, SHADOW,
                (pose, consumer) -> shadow(consumer, pose, o, e1, e2));

        List<GlowPass.Layer> layers = new ArrayList<>();
        // Under the disk and jets, depth, so later clouds and weather don't cover them.
        List<GlowPass.Layer> depth = new ArrayList<>();
        if (AccretionDisk.ready()) {
            AccretionDisk.View view = AccretionDisk.view(o.x, o.y, o.z);
            for (AccretionDisk.Image image : view.images) {
                float lift = image.secondary ? SECONDARY_LIFT : PRIMARY_LIFT;
                SubmitNodeCollector.CustomGeometryRenderer quads =
                        (pose, consumer) -> disk(consumer, pose, o, view, image, lift, seconds);
                layers.add(glowing(DISK, quads));
                depth.add(new GlowPass.Layer(GlowPass.DEPTH_ONLY, DISK, quads));
                layers.add(glowing(SWIRLS,
                        (pose, consumer) -> swirls(consumer, pose, o, view, image, lift + SWIRL_LIFT, seconds)));
            }
        }
        if (jets > 0) {
            float reach = jets * jets * (3 - 2 * jets); // ease in and out
            SubmitNodeCollector.CustomGeometryRenderer quads = (pose, consumer) -> jets(consumer, pose, o, e1, reach, seconds);
            layers.add(glowing(JET, quads));
            depth.add(new GlowPass.Layer(GlowPass.DEPTH_ONLY, JET, quads));
            layers.add(glowing(GLOW, (pose, consumer) -> poleGlow(consumer, pose, o, e1, e2, reach)));
        }
        if (inWorld) {
            layers.addAll(depth);
            collector.submitParticleGroup(new GlowPass(poseStack.last(), layers));
        } else {
            for (int i = 0; i < layers.size(); i++) {
                GlowPass.Layer layer = layers.get(i);
                collector.order(i + 1).submitCustomGeometry(poseStack, RenderTypes.eyes(layer.texture()), layer.quads());
            }
        }
        poseStack.popPose();
    }

    private static GlowPass.Layer glowing(Identifier texture, SubmitNodeCollector.CustomGeometryRenderer quads) {
        return new GlowPass.Layer(RenderPipelines.EYES, texture, quads);
    }

    // ── Shadow ────────────────────────────────────────────────────────────────────

    private static void shadow(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, Vector3f e1, Vector3f e2) {
        float r = AccretionDisk.SHADOW_RADIUS;
        for (int i = 0; i < SHADOW_SEGMENTS; i++) {
            float a0 = Mth.TWO_PI * i / SHADOW_SEGMENTS, a1 = Mth.TWO_PI * (i + 1) / SHADOW_SEGMENTS;
            float x0 = r * (Mth.cos(a0) * e1.x + Mth.sin(a0) * e2.x);
            float y0 = r * (Mth.cos(a0) * e1.y + Mth.sin(a0) * e2.y);
            float z0 = r * (Mth.cos(a0) * e1.z + Mth.sin(a0) * e2.z);
            float x1 = r * (Mth.cos(a1) * e1.x + Mth.sin(a1) * e2.x);
            float y1 = r * (Mth.cos(a1) * e1.y + Mth.sin(a1) * e2.y);
            float z1 = r * (Mth.cos(a1) * e1.z + Mth.sin(a1) * e2.z);
            vertex(consumer, pose, o, 0, 0, 0, 0xFFFFFFFF, 0.5f, 0.5f);
            vertex(consumer, pose, o, x0, y0, z0, 0xFFFFFFFF, 0.5f, 0.5f);
            vertex(consumer, pose, o, x1, y1, z1, 0xFFFFFFFF, 0.5f, 0.5f);
            vertex(consumer, pose, o, x1, y1, z1, 0xFFFFFFFF, 0.5f, 0.5f);
        }
    }

    // ── Disk ──────────────────────────────────────────────────────────────────────

    private static void disk(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, AccretionDisk.View view,
                             AccretionDisk.Image image, float lift, float seconds) {
        float turn = (DISK_TURN * seconds) % 1f;
        diskQuads(consumer, pose, o, view, image, lift, image.colour, false, 1f, ring -> turn, 0f);
    }

    /** The two copies of the swirls, each fading in and out over its cycle, half a cycle apart. */
    private static void swirls(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, AccretionDisk.View view,
                               AccretionDisk.Image image, float lift, float seconds) {
        for (int copy = 0; copy < 2; copy++) {
            float phase = seconds / SWIRL_CYCLE + copy * 0.5f;
            float cycle = (float) Math.floor(phase);
            float age = phase - cycle;
            float weight = 1 - Math.abs(2 * age - 1);
            if (weight <= 0.01f) continue;
            float elapsed = age * SWIRL_CYCLE;
            // A fresh start each cycle, so the reset doesn't show the same pattern again.
            float shift = (cycle * 0.618f + copy * 0.37f) % 1f;
            diskQuads(consumer, pose, o, view, image, lift, image.glow, true, weight,
                    ring -> elapsed * swirlTurn(ring), shift);
        }
    }

    /** Turns per second of the swirls on a ring: Kepler's law, faster toward the middle. */
    private static float swirlTurn(int ring) {
        float s = (float) ring / (AccretionDisk.RINGS - 1);
        float radius = AccretionDisk.SHADOW_RADIUS + (AccretionDisk.OUTER_RADIUS - AccretionDisk.SHADOW_RADIUS) * s;
        return INNER_TURN * (float) Math.pow(AccretionDisk.SHADOW_RADIUS / radius, 1.5);
    }

    private interface RingTurn {
        float turned(int ring);
    }

    /**
     * Draws an image of the disk. {@code colours} are its vertices' colours, scaled by
     * {@code weight} (see {@link #colour}); the texture has turned {@code turn} of the way round on
     * each ring.
     */
    private static void diskQuads(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, AccretionDisk.View view,
                                  AccretionDisk.Image image, float lift, int[] colours, boolean glowing,
                                  float weight, RingTurn turn, float shift) {
        int segments = AccretionDisk.SEGMENTS;
        float[] p = image.position;
        for (int ring = 0; ring < AccretionDisk.RINGS - 1; ring++) {
            float v0 = (float) ring / (AccretionDisk.RINGS - 1);
            float v1 = (float) (ring + 1) / (AccretionDisk.RINGS - 1);
            float turn0 = turn.turned(ring), turn1 = turn.turned(ring + 1);
            for (int seg = 0; seg < segments; seg++) {
                int next = (seg + 1) % segments;
                float angle0 = view.angle[seg];
                float angle1 = next == 0 ? view.angle[0] + Mth.TWO_PI : view.angle[next];
                int a = AccretionDisk.index(ring, seg), b = AccretionDisk.index(ring, next);
                int c = AccretionDisk.index(ring + 1, next), d = AccretionDisk.index(ring + 1, seg);
                CORNERS[0].set(p, a, o, lift, colour(colours[a], weight, glowing), u(angle0, turn0, shift), v0);
                CORNERS[1].set(p, b, o, lift, colour(colours[b], weight, glowing), u(angle1, turn0, shift), v0);
                CORNERS[2].set(p, c, o, lift, colour(colours[c], weight, glowing), u(angle1, turn1, shift), v1);
                CORNERS[3].set(p, d, o, lift, colour(colours[d], weight, glowing), u(angle0, turn1, shift), v1);
                quad(consumer, pose, o);
            }
        }
    }

    private static float u(float angle, float turned, float shift) {
        return SWIRL_REPEATS * (angle / Mth.TWO_PI - turned) + shift;
    }

    /**
     * A colour's red, green and blue scaled by {@code weight}, keeping its alpha, or as a
     * {@link #glow} if {@code glowing}.
     */
    private static int colour(int argb, float weight, boolean glowing) {
        int r = Math.round(((argb >> 16) & 0xFF) * weight);
        int g = Math.round(((argb >> 8) & 0xFF) * weight);
        int b = Math.round((argb & 0xFF) * weight);
        int rgb = r << 16 | g << 8 | b;
        return glowing ? glow(rgb) : argb & 0xFF000000 | rgb;
    }

    /**
     * A glow, as a colour to blend toward: its hue at full brightness, with its brightness as its
     * opacity. Over darker things that's much the same as adding it, and it never darkens.
     */
    private static int glow(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int brightest = Math.max(r, Math.max(g, b));
        if (brightest == 0) return 0;
        return brightest << 24 | (r * 255 / brightest) << 16 | (g * 255 / brightest) << 8 | b * 255 / brightest;
    }

    // ── Jets ──────────────────────────────────────────────────────────────────────

    /** Both jets, each a wide blue sheath round a narrow white core, streaming outward. */
    private static void jets(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, Vector3f e1, float reach,
                             float seconds) {
        // Turned about the jets' axis to face the viewer; looking straight down it, any way will do.
        Vector3f side = new Vector3f(0, 1, 0).cross(o);
        if (side.lengthSquared() < 1e-6f) side.set(e1);
        side.normalize();
        float flow = (JET_FLOW * seconds) % 1f;
        float length = JET_START + (JET_LENGTH - JET_START) * reach;
        for (int up = -1; up <= 1; up += 2) {
            strip(consumer, pose, o, side, up, length, reach, flow, 0.9f, 1.9f, JET_SHEATH, 0.7f);
            strip(consumer, pose, o, side, up, length, reach, flow, 0.35f, 0.6f, JET_CORE, 1f);
        }
    }

    /**
     * One layer of one jet: a strip from {@link #JET_START} to {@code length} along {@code up},
     * {@code baseWidth} wide at the hole widening by {@code spread} at full length, brightest
     * along the middle and fading to nothing at its edges and tip.
     */
    private static void strip(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, Vector3f side, int up,
                              float length, float reach, float flow, float baseWidth, float spread, float[] rgb,
                              float strength) {
        for (int i = 0; i < JET_SEGMENTS; i++) {
            float t0 = (float) i / JET_SEGMENTS, t1 = (float) (i + 1) / JET_SEGMENTS;
            float y0 = JET_START + (length - JET_START) * t0, y1 = JET_START + (length - JET_START) * t1;
            float w0 = baseWidth + spread * y0 / JET_LENGTH, w1 = baseWidth + spread * y1 / JET_LENGTH;
            int c0 = jetColour(rgb, strength * reach * (1 - t0 * t0) * fadeIn(y0));
            int c1 = jetColour(rgb, strength * reach * (1 - t1 * t1) * fadeIn(y1));
            // The edges are the same colour, but clear.
            int edge = jetColour(rgb, 1) & 0xFFFFFF;
            float v0 = y0 / JET_LENGTH - flow, v1 = y1 / JET_LENGTH - flow;
            for (int half = -1; half <= 1; half += 2) {
                // From the middle of the strip out to one edge.
                CORNERS[0].set(0, up * y0, 0, c0, 0.5f, v0);
                CORNERS[1].set(half * w0 * side.x, up * y0, half * w0 * side.z, edge, 0.5f + half * 0.5f, v0);
                CORNERS[2].set(half * w1 * side.x, up * y1, half * w1 * side.z, edge, 0.5f + half * 0.5f, v1);
                CORNERS[3].set(0, up * y1, 0, c1, 0.5f, v1);
                quad(consumer, pose, o);
            }
        }
    }

    private static float fadeIn(float y) {
        float t = Math.min(1, (y - JET_START) / JET_FADE_IN);
        return t * t * (3 - 2 * t);
    }

    private static int jetColour(float[] rgb, float intensity) {
        float k = Math.max(0, Math.min(1, intensity));
        return glow(Math.round(rgb[0] * k * 255) << 16 | Math.round(rgb[1] * k * 255) << 8
                | Math.round(rgb[2] * k * 255));
    }

    /** Looking down a jet, it shows as a bright spot over the pole. */
    private static void poleGlow(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, Vector3f e1, Vector3f e2,
                                 float reach) {
        float along = (Math.abs(o.y) - 0.6f) / 0.4f;
        if (along <= 0) return;
        int colour = jetColour(JET_CORE, reach * along * along);
        float y = Math.signum(o.y) * 3f, size = 5f;
        CORNERS[0].set(-size * (e1.x + e2.x), y - size * (e1.y + e2.y), -size * (e1.z + e2.z), colour, 0, 0);
        CORNERS[1].set(size * (e1.x - e2.x), y + size * (e1.y - e2.y), size * (e1.z - e2.z), colour, 1, 0);
        CORNERS[2].set(size * (e1.x + e2.x), y + size * (e1.y + e2.y), size * (e1.z + e2.z), colour, 1, 1);
        CORNERS[3].set(-size * (e1.x - e2.x), y - size * (e1.y - e2.y), -size * (e1.z - e2.z), colour, 0, 1);
        quad(consumer, pose, o);
    }

    // ── Vertices ──────────────────────────────────────────────────────────────────

    /** A quad's corners, filled in before {@link #quad}. Only ever used on the render thread. */
    private static final Corner[] CORNERS = {new Corner(), new Corner(), new Corner(), new Corner()};

    private static final class Corner {
        float x, y, z, u, v;
        int colour;

        void set(float x, float y, float z, int colour, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.colour = colour;
            this.u = u;
            this.v = v;
        }

        /** Vertex {@code i} of an image, lifted {@code lift} toward the viewer. */
        void set(float[] position, int i, Vector3f o, float lift, int colour, float u, float v) {
            set(position[i * 3] + lift * o.x, position[i * 3 + 1] + lift * o.y, position[i * 3 + 2] + lift * o.z,
                    colour, u, v);
        }
    }

    /** Draws {@link #CORNERS} as a quad facing the viewer, whichever way round they were given. */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o) {
        Corner a = CORNERS[0], b = CORNERS[1], c = CORNERS[2], d = CORNERS[3];
        // The quad's normal from its diagonals; if it faces away, wind it the other way.
        float ux = c.x - a.x, uy = c.y - a.y, uz = c.z - a.z;
        float vx = d.x - b.x, vy = d.y - b.y, vz = d.z - b.z;
        float facing = (uy * vz - uz * vy) * o.x + (uz * vx - ux * vz) * o.y + (ux * vy - uy * vx) * o.z;
        if (facing >= 0) {
            vertex(consumer, pose, o, a);
            vertex(consumer, pose, o, b);
            vertex(consumer, pose, o, c);
            vertex(consumer, pose, o, d);
        } else {
            vertex(consumer, pose, o, d);
            vertex(consumer, pose, o, c);
            vertex(consumer, pose, o, b);
            vertex(consumer, pose, o, a);
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, Corner corner) {
        vertex(consumer, pose, o, corner.x, corner.y, corner.z, corner.colour, corner.u, corner.v);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f o, float x, float y, float z,
                               int colour, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(colour)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, o.x, o.y, o.z);
    }
}
