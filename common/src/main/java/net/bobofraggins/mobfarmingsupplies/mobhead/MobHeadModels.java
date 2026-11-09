package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * The head of a mob, as drawn by Mob Heads: the {@code head} part of the mob's own model (with its
 * ears, horns, snout, ...) and the mob's own texture — nothing is copied out of the game, so a head
 * always matches its mob.
 *
 * <p>Each head is a fresh copy baked from the mob's model layer ({@code <mob id>#main}), never the
 * part of the model its renderer animates, so it can't be caught mid-animation. The texture and
 * render type come from the mob's renderer, via a throwaway instance of the mob.
 *
 * <p>A few mobs need more than their base texture to look like themselves; {@link #build} adds
 * those layers by hand (Enderman eyes, Sheep wool, the Drowned's outer skin, the Slime's jelly,
 * the Sulfur Cube's inner cube, the Mooshroom's mushroom), the way the mob's own render layers
 * draw them. Mobs that are all head (slimes, sulfur cubes, ghasts) are drawn whole, less a ghast's
 * dangling tentacles. A strider's is its body, and a fish's or bug's is the front third of it
 * ({@link #cutBehind}). A nautilus's is the front two-thirds of what's under its shell, back to
 * just behind its eyes ({@link #fillNautilusHoles}).
 */
public final class MobHeadModels {

    /** Vanilla heads are 8 pixels across; every Mob Head is scaled so its largest side matches. */
    private static final float HEAD_SIZE = 0.5f;

    private static final Identifier ENDERMAN_EYES = Identifier.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png");
    private static final Identifier SHEEP_WOOL = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
    private static final Identifier SLIME = Identifier.withDefaultNamespace("textures/entity/slime/slime.png");
    private static final Identifier SULFUR_CUBE_INNER = Identifier.withDefaultNamespace("textures/entity/sulfur_cube/sulfur_cube_inner.png");
    private static final Identifier DROWNED_OUTER = Identifier.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");
    private static final Identifier KOB_PATTERN = Identifier.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_1.png");

    private static final int FULL_BRIGHT = 0xF000F0;
    /** The inside of a nautilus's mouth on its texture (texels of the 128-wide sheet), a plain dark red. */
    private static final float MOUTH_U0 = 3f / 128f, MOUTH_V0 = 78f / 128f, MOUTH_U1 = 7f / 128f, MOUTH_V1 = 81f / 128f;
    /** The order {@code ModelPart.Cube} builds its faces in. */
    private static final int FACE_DOWN = 0, FACE_UP = 1, FACE_NORTH = 3, FACE_SOUTH = 5;

    /** A cut head's cubes thinner than this are fins or antennae: they stick out rather than count towards its size. */
    private static final float MIN_SIZED_THICKNESS = 2f;

    /** One pass of a head: a model part drawn with a render type and tint (-1 = none). */
    public record Layer(ModelPart part, RenderType renderType, int tint) {}

    /**
     * A ready-to-draw head: its layers, bottom first, and optionally the mushroom a Mooshroom
     * wears on its head. Its bounds are in the model's own space (block units, y down, facing
     * -z); {@link #submit} centres it, stands it on y = 0 and scales it to vanilla head size.
     * One that {@code glows} ignores the light around it, as a Glow Squid does.
     */
    public record Head(List<Layer> layers, @Nullable BlockModelRenderState mushroom,
                       float scale, float centreX, float bottomY, float centreZ, boolean glows) {}

    private static final Map<EntityType<?>, Optional<Head>> CACHE = new HashMap<>();
    private static int nextDummyId = 1;

    private MobHeadModels() {}

    /** The head for {@code type}, or null if it can't be drawn (no level yet, no head part, ...). */
    @Nullable
    public static Head get(EntityType<?> type) {
        Optional<Head> cached = CACHE.get(type);
        if (cached != null) return cached.orElse(null);
        if (Minecraft.getInstance().level == null) return null; // try again once a world is loaded
        Optional<Head> built;
        try {
            built = Optional.ofNullable(build(type));
        } catch (RuntimeException e) {
            built = Optional.empty();
        }
        CACHE.put(type, built);
        return built.orElse(null);
    }

    /** Drops every cached head (models are rebaked on a resource reload). */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * Draws {@code type}'s head with its base on y = 0, centred on x and z, facing -z — the same
     * space a vanilla skull model is drawn in after its block or item transform.
     */
    public static void submit(EntityType<?> type, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submit(type, poseStack, collector, light, -1);
    }

    /** As {@link #submit(EntityType, PoseStack, SubmitNodeCollector, int)}, every layer tinted {@code tint} (-1 = none). */
    public static void submit(EntityType<?> type, PoseStack poseStack, SubmitNodeCollector collector, int worldLight, int tint) {
        Head head = get(type);
        if (head == null) return;
        int light = head.glows() ? FULL_BRIGHT : worldLight;
        poseStack.pushPose();
        poseStack.scale(head.scale(), head.scale(), head.scale());
        poseStack.translate(-head.centreX(), -head.bottomY(), -head.centreZ());
        for (Layer layer : head.layers()) {
            collector.submitModelPart(layer.part(), poseStack, layer.renderType(), light, OverlayTexture.NO_OVERLAY,
                    null, tint == -1 ? layer.tint() : layer.tint() == -1 ? tint : ARGB.multiply(layer.tint(), tint));
        }
        if (head.mushroom() != null) {
            // Where MushroomCowMushroomLayer puts the mushroom on the head.
            head.layers().getFirst().part().translateAndRotate(poseStack);
            poseStack.translate(0f, -0.7f, -0.2f);
            poseStack.rotate(Axis.YP.rotationDegrees(-78f));
            poseStack.scale(-1f, -1f, 1f);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            head.mushroom().submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Head build(EntityType<?> type) {
        Minecraft mc = Minecraft.getInstance();
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        ModelPart part = bakeHead(layerFor(id), headPartFor(id));
        if (part == null) return null;

        Entity dummy = type.create(mc.level, EntitySpawnReason.LOAD);
        if (!(dummy instanceof LivingEntity living)) return null;
        dummy.setId(nextDummyId++); // never added to a level, which is what assigns IDs
        if (!(mc.getEntityRenderDispatcher().getRenderer(living) instanceof LivingEntityRenderer renderer)) return null;
        LivingEntityRenderState state = (LivingEntityRenderState) renderer.createRenderState();
        renderer.extractRenderState(living, state, 0f);
        Identifier texture = renderer.getTextureLocation(state);
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(part, renderer.getModel().renderType(texture), -1));
        BlockModelRenderState mushroom = null;
        ModelPart sizedBy = part;
        switch (id.getPath()) {
            case "enderman" -> layers.add(new Layer(part, RenderTypes.eyes(ENDERMAN_EYES), -1));
            case "sheep" -> {
                ModelPart wool = bakeHead(ModelLayers.SHEEP_WOOL, "head");
                if (wool != null) layers.add(new Layer(wool, RenderTypes.entityCutout(SHEEP_WOOL),
                        state instanceof SheepRenderState sheep ? sheep.getWoolColor() : -1));
            }
            case "drowned" -> {
                ModelPart outer = bakeHead(ModelLayers.DROWNED_OUTER_LAYER, "head");
                if (outer != null) layers.add(new Layer(outer, RenderTypes.entityCutout(DROWNED_OUTER), -1));
            }
            case "slime" -> {
                ModelPart outer = bakeHead(ModelLayers.SLIME_OUTER, null);
                layers.add(new Layer(outer, RenderTypes.entityTranslucent(SLIME), -1));
                sizedBy = outer; // the jelly, not the core inside it
            }
            case "sulfur_cube" -> {
                // Inside out from a slime: the model is the shell, with a cube floating in it.
                ModelPart inner = bakeHead(ModelLayers.SULFUR_CUBE_INNER, null);
                if (inner != null) layers.add(0, new Layer(inner, RenderTypes.entityTranslucent(SULFUR_CUBE_INNER), -1));
            }
            case "mooshroom" -> {
                if (state instanceof MushroomCowRenderState cow && !cow.mushroomModel.isEmpty()) mushroom = cow.mushroomModel;
            }
            case "nautilus", "zombie_nautilus" -> {
                part.yRot = (float) Math.PI; // its mouth is at +z, and heads face -z
                fillNautilusHoles(part);
            }
            case "bee" -> {
                // They start just behind where it's cut, and would leave a sliver out each side.
                ModelPart bone = part.getChild("bone");
                bone.getChild("right_wing").visible = false;
                bone.getChild("left_wing").visible = false;
            }
            case "tropical_fish" -> {
                // The one type of head stands for every pattern: a clownfish, the best known.
                layers.set(0, new Layer(part, layers.getFirst().renderType(), DyeColor.ORANGE.getTextureDiffuseColor()));
                ModelPart pattern = bakeHead(ModelLayers.TROPICAL_FISH_SMALL_PATTERN, null);
                if (pattern != null) layers.add(new Layer(pattern, RenderTypes.entityCutout(KOB_PATTERN),
                        DyeColor.WHITE.getTextureDiffuseColor()));
            }
            default -> {}
        }

        // Size the head by its own solid cubes, not its children or flat planes: guardian spikes,
        // warden tendrils and creaking branches would otherwise shrink the head itself to a speck.
        // They stick out instead, as the Ender Dragon head's horns do. A head part with no such
        // cubes uses them all.
        float[] min = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] max = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        Float cut = cutFor(id);
        if (cut != null) {
            for (Layer layer : layers) cutBehind(layer.part(), cut, min, max);
        } else if (!bounds(sizedBy, true, min, max)) {
            bounds(sizedBy, false, min, max);
        }
        float size = Math.max(max[0] - min[0], Math.max(max[1] - min[1], max[2] - min[2]));
        if (!(size > 0)) return null;
        return new Head(List.copyOf(layers), mushroom, HEAD_SIZE / size,
                (min[0] + max[0]) / 2, max[1], (min[2] + max[2]) / 2, // y points down: max y is the base
                id.getPath().equals("glow_squid"));
    }

    /** The model layer a mob's head comes from: its own, except where it borrows another mob's. */
    private static ModelLayerLocation layerFor(Identifier id) {
        return switch (id.getPath()) {
            case "camel_husk" -> ModelLayers.CAMEL; // drawn with the camel model and its own texture
            case "pufferfish" -> ModelLayers.PUFFERFISH_BIG; // puffed up: its small form is barely there
            case "tropical_fish" -> ModelLayers.TROPICAL_FISH_SMALL;
            default -> new ModelLayerLocation(id, "main");
        };
    }

    /**
     * The model part that is the mob's head, or null for the whole model: slimes and sulfur cubes are
     * all head, and so is a ghast's, squid's or strider's body. Fish and bugs are whole until
     * {@link #cutBehind} is done.
     */
    @Nullable
    private static String headPartFor(Identifier id) {
        return switch (id.getPath()) {
            case "ghast", "happy_ghast", "squid", "glow_squid", "strider", "nautilus", "zombie_nautilus" -> "body";
            case "slime", "magma_cube", "sulfur_cube", "bee", "cod", "endermite", "pufferfish", "salmon", "silverfish",
                 "tadpole", "tropical_fish" -> null;
            default -> "head";
        };
    }

    /**
     * Where a fish's or bug's head ends: the model z (in pixels; they face -z) about a third of
     * the way along its body, or null for a mob that isn't cut.
     */
    @Nullable
    private static Float cutFor(Identifier id) {
        return switch (id.getPath()) {
            case "cod", "tadpole" -> 0f;
            case "bee" -> -2f;
            case "salmon" -> -4f;
            case "endermite" -> -1.5f;
            case "nautilus", "zombie_nautilus" -> 10.3f; // just behind its eyes
            case "silverfish" -> 1f;
            case "pufferfish", "tropical_fish" -> -1f;
            default -> null;
        };
    }

    /**
     * Cuts off everything of {@code root} behind {@code cutZ}. A cube across it loses its back:
     * the faces along it are shortened (with their textures, so nothing is squashed) and its back
     * face moves up to the cut to close it. A cube wholly behind it collapses to nothing. Grows
     * {@code min} / {@code max} (block units) to what's left, not counting fins.
     */
    private static void cutBehind(ModelPart root, float cutZ, float[] min, float[] max) {
        Vector3f corner = new Vector3f();
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            // Which way the part's own z runs in the model (a nautilus is turned around), and the
            // cut in the part's own space, from its origin in pixels.
            float direction = pose.pose().m22() < 0f ? -1f : 1f;
            float localCut = direction * (cutZ - pose.pose().m32() * 16f);
            float nearest = direction > 0f ? cube.minZ : cube.maxZ;
            if (direction * (nearest - localCut) >= 0f) {
                collapse(cube);
                return;
            }
            for (int i = 0; i < cube.polygons.length; i++) {
                cube.polygons[i] = shortened(cube.polygons[i], localCut, direction);
            }
            if (flat(cube) || Math.min(cube.maxX - cube.minX, cube.maxY - cube.minY) < MIN_SIZED_THICKNESS) return;
            for (ModelPart.Polygon polygon : cube.polygons) {
                for (ModelPart.Vertex vertex : polygon.vertices()) {
                    corner.set(vertex.worldX(), vertex.worldY(), vertex.worldZ());
                    pose.pose().transformPosition(corner);
                    for (int axis = 0; axis < 3; axis++) {
                        min[axis] = Math.min(min[axis], corner.get(axis));
                        max[axis] = Math.max(max[axis], corner.get(axis));
                    }
                }
            }
        });
    }

    /** Leaves {@code cube} with no area to draw. */
    private static void collapse(ModelPart.Cube cube) {
        ModelPart.Vertex point = new ModelPart.Vertex(0f, 0f, 0f, 0f, 0f);
        for (int i = 0; i < cube.polygons.length; i++) {
            ModelPart.Polygon polygon = cube.polygons[i];
            ModelPart.Vertex[] vertices = new ModelPart.Vertex[polygon.vertices().length];
            java.util.Arrays.fill(vertices, point);
            cube.polygons[i] = new ModelPart.Polygon(vertices, polygon.normal());
        }
    }

    /**
     * Fills the holes Mojang left in a nautilus, which its shell normally hides: its body's back
     * (which closes the cut), each lip's and tongue's ends and the faces where the lips meet have
     * no texture, so they're given the mouth's dark red. The front ends are set back a pixel from
     * where the lips start, leaving the jagged rims of the lips standing proud of them.
     */
    private static void fillNautilusHoles(ModelPart body) {
        body.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (index != 0) return; // the body's second cube is the plate the mouth is fixed to
            ModelPart.Polygon[] faces = cube.polygons;
            if (path.isEmpty()) {
                faces[FACE_NORTH] = filled(faces[FACE_NORTH]);
                return;
            }
            faces[FACE_NORTH] = filled(faces[FACE_NORTH]);
            faces[FACE_SOUTH] = filled(setBack(faces[FACE_SOUTH], cube.minZ + 1f));
            if (path.endsWith("upper_mouth")) faces[FACE_UP] = filled(faces[FACE_UP]);
            if (path.endsWith("lower_mouth")) faces[FACE_DOWN] = filled(faces[FACE_DOWN]);
        });
    }

    /** {@code polygon} moved to {@code z}, its texture unchanged. */
    private static ModelPart.Polygon setBack(ModelPart.Polygon polygon, float z) {
        ModelPart.Vertex[] vertices = polygon.vertices().clone();
        for (int i = 0; i < vertices.length; i++) {
            ModelPart.Vertex vertex = vertices[i];
            vertices[i] = new ModelPart.Vertex(vertex.x(), vertex.y(), z, vertex.u(), vertex.v());
        }
        return new ModelPart.Polygon(vertices, polygon.normal());
    }

    /** {@code polygon} textured with the inside of the mouth. */
    private static ModelPart.Polygon filled(ModelPart.Polygon polygon) {
        ModelPart.Vertex[] old = polygon.vertices();
        return new ModelPart.Polygon(new ModelPart.Vertex[]{
                old[0].remap(MOUTH_U1, MOUTH_V0), old[1].remap(MOUTH_U0, MOUTH_V0),
                old[2].remap(MOUTH_U0, MOUTH_V1), old[3].remap(MOUTH_U1, MOUTH_V1)}, polygon.normal());
    }

    /** {@code polygon} with every corner behind {@code localCut} brought up to it. */
    private static ModelPart.Polygon shortened(ModelPart.Polygon polygon, float localCut, float direction) {
        ModelPart.Vertex[] vertices = polygon.vertices().clone();
        for (int i = 0; i < vertices.length; i++) {
            ModelPart.Vertex vertex = vertices[i];
            if (direction * (vertex.z() - localCut) <= 0f) continue;
            float u = vertex.u(), v = vertex.v();
            // A face along the cut keeps its texture in place: the corner's u and v slide with it.
            for (ModelPart.Vertex front : polygon.vertices()) {
                if (direction * (front.z() - localCut) < 0f && front.x() == vertex.x() && front.y() == vertex.y()) {
                    float t = (localCut - front.z()) / (vertex.z() - front.z());
                    u = front.u() + (vertex.u() - front.u()) * t;
                    v = front.v() + (vertex.v() - front.v()) * t;
                }
            }
            vertices[i] = new ModelPart.Vertex(vertex.x(), vertex.y(), localCut, u, v);
        }
        return new ModelPart.Polygon(vertices, polygon.normal());
    }

    /**
     * A fresh copy of {@code partName} (null = the whole model) baked from {@code layer}, or null
     * if the layer has no such part. Ghast and squid tentacles are hidden: a head shouldn't dangle.
     */
    @Nullable
    private static ModelPart bakeHead(ModelLayerLocation layer, @Nullable String partName) {
        ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(layer);
        var lookup = root.createPartLookup();
        for (int i = 0; ; i++) {
            ModelPart tentacle = lookup.apply("tentacle" + i);
            if (tentacle == null) break;
            tentacle.visible = false;
        }
        return partName == null ? root : lookup.apply(partName);
    }

    /**
     * Grows {@code min} / {@code max} (block units, the head's own pose applied) to the cubes of
     * {@code part} — only its own solid ones if {@code ownOnly}, else all of them. Returns whether any
     * cube was found.
     */
    private static boolean bounds(ModelPart part, boolean ownOnly, float[] min, float[] max) {
        boolean[] found = {false};
        Vector3f corner = new Vector3f();
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (ownOnly && (!path.isEmpty() || flat(cube))) return;
            if (path.contains("tentacle")) return; // hidden, see bakeHead
            found[0] = true;
            for (int c = 0; c < 8; c++) {
                corner.set(((c & 1) == 0 ? cube.minX : cube.maxX) / 16f,
                        ((c & 2) == 0 ? cube.minY : cube.maxY) / 16f,
                        ((c & 4) == 0 ? cube.minZ : cube.maxZ) / 16f);
                pose.pose().transformPosition(corner);
                min[0] = Math.min(min[0], corner.x); min[1] = Math.min(min[1], corner.y); min[2] = Math.min(min[2], corner.z);
                max[0] = Math.max(max[0], corner.x); max[1] = Math.max(max[1], corner.y); max[2] = Math.max(max[2], corner.z);
            }
        });
        return found[0];
    }

    /** A zero-thickness plane (branches, flat ears). */
    private static boolean flat(ModelPart.Cube cube) {
        return cube.minX == cube.maxX || cube.minY == cube.maxY || cube.minZ == cube.maxZ;
    }
}
