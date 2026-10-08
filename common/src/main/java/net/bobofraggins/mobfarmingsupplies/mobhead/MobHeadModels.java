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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * the Mooshroom's mushroom), the way the mob's own render layers draw them. Mobs that are all head
 * (slimes, ghasts) are drawn whole, less a ghast's dangling tentacles.
 */
public final class MobHeadModels {

    /** Vanilla heads are 8 pixels across; every Mob Head is scaled so its largest side matches. */
    private static final float HEAD_SIZE = 0.5f;

    private static final Identifier ENDERMAN_EYES = Identifier.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png");
    private static final Identifier SHEEP_WOOL = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
    private static final Identifier SLIME = Identifier.withDefaultNamespace("textures/entity/slime/slime.png");
    private static final Identifier DROWNED_OUTER = Identifier.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");

    /** One pass of a head: a model part drawn with a render type and tint (-1 = none). */
    public record Layer(ModelPart part, RenderType renderType, int tint) {}

    /**
     * A ready-to-draw head: its layers, bottom first, and optionally the mushroom a Mooshroom
     * wears on its head. Its bounds are in the model's own space (block units, y down, facing
     * -z); {@link #submit} centres it, stands it on y = 0 and scales it to vanilla head size.
     */
    public record Head(List<Layer> layers, @Nullable BlockModelRenderState mushroom,
                       float scale, float centreX, float bottomY, float centreZ) {}

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
        Head head = get(type);
        if (head == null) return;
        poseStack.pushPose();
        poseStack.scale(head.scale(), head.scale(), head.scale());
        poseStack.translate(-head.centreX(), -head.bottomY(), -head.centreZ());
        for (Layer layer : head.layers()) {
            collector.submitModelPart(layer.part(), poseStack, layer.renderType(), light, OverlayTexture.NO_OVERLAY,
                    null, layer.tint());
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
            case "mooshroom" -> {
                if (state instanceof MushroomCowRenderState cow && !cow.mushroomModel.isEmpty()) mushroom = cow.mushroomModel;
            }
            default -> {}
        }

        // Size the head by its own solid cubes, not its children or flat planes: guardian spikes,
        // warden tendrils and creaking branches would otherwise shrink the head itself to a speck.
        // They stick out instead, as the Ender Dragon head's horns do. A head part with no such
        // cubes uses them all.
        float[] min = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] max = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        if (!bounds(sizedBy, true, min, max)) bounds(sizedBy, false, min, max);
        float size = Math.max(max[0] - min[0], Math.max(max[1] - min[1], max[2] - min[2]));
        if (!(size > 0)) return null;
        return new Head(List.copyOf(layers), mushroom, HEAD_SIZE / size,
                (min[0] + max[0]) / 2, max[1], (min[2] + max[2]) / 2); // y points down: max y is the base
    }

    /** The model layer a mob's head comes from: its own, except where it borrows another mob's. */
    private static ModelLayerLocation layerFor(Identifier id) {
        return switch (id.getPath()) {
            case "camel_husk" -> ModelLayers.CAMEL; // drawn with the camel model and its own texture
            default -> new ModelLayerLocation(id, "main");
        };
    }

    /**
     * The model part that is the mob's head, or null for the whole model: slimes and ghasts are
     * all head.
     */
    @Nullable
    private static String headPartFor(Identifier id) {
        return switch (id.getPath()) {
            case "ghast", "happy_ghast" -> "body";
            case "slime", "magma_cube" -> null;
            default -> "head";
        };
    }

    /**
     * A fresh copy of {@code partName} (null = the whole model) baked from {@code layer}, or null
     * if the layer has no such part. Ghast tentacles are hidden: a head shouldn't dangle.
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
