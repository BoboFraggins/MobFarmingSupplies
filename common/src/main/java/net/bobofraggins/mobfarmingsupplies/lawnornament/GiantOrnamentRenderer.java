package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws the giant lawn ornaments: a vanilla humanoid model at {@value #SCALE} times life size, so
 * four blocks tall, standing on its block and facing the way it was placed.
 */
public class GiantOrnamentRenderer
        implements BlockEntityRenderer<GiantOrnamentBlockEntity, GiantOrnamentRenderer.State> {

    /** A humanoid is about two blocks tall; this makes it four. */
    static final float SCALE = 2.0f;

    private static final Identifier STEVE = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final Identifier ALEX = Identifier.withDefaultNamespace("textures/entity/player/slim/alex.png");

    /** Which vanilla model an ornament uses, and the texture drawn on it. */
    record Figure(ModelLayerLocation layer, Identifier texture) {}

    /** The look of each ornament; a Giant Player Lawn Ornament's is its owner's ({@link #look}). */
    static Figure figure(GiantOrnament ornament) {
        return switch (ornament) {
            case SKELETON -> new Figure(ModelLayers.SKELETON,
                    Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
            case HEROBRINE -> new Figure(ModelLayers.PLAYER,
                    Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID,
                            "textures/entity/player/wide/herobrine.png"));
            case PLAYER -> new Figure(ModelLayers.PLAYER, STEVE);
        };
    }

    /** Every ornament's model, plus the slim-armed player model for owners with slim skins. */
    record Models(Map<GiantOrnament, GiantFigureModel> byOrnament, GiantFigureModel slimPlayer) {

        static Models bake(Function<ModelLayerLocation, ModelPart> bakeLayer) {
            Map<GiantOrnament, GiantFigureModel> models = new EnumMap<>(GiantOrnament.class);
            for (GiantOrnament ornament : GiantOrnament.values()) {
                Figure figure = figure(ornament);
                models.put(ornament, new GiantFigureModel(bakeLayer.apply(figure.layer()), figure.texture()));
            }
            return new Models(models, new GiantFigureModel(bakeLayer.apply(ModelLayers.PLAYER_SLIM), ALEX));
        }

        /** The model to draw, and how it's textured. */
        Look look(GiantOrnament ornament, @Nullable ResolvableProfile owner, boolean alexWhenUnowned) {
            if (ornament != GiantOrnament.PLAYER) {
                GiantFigureModel model = byOrnament.get(ornament);
                return new Look(model, model.renderType(model.texture()));
            }
            if (owner == null) {
                GiantFigureModel model = alexWhenUnowned ? slimPlayer : byOrnament.get(ornament);
                return new Look(model, model.renderType(alexWhenUnowned ? ALEX : STEVE));
            }
            PlayerSkinRenderCache.RenderInfo skin = Minecraft.getInstance().playerSkinRenderCache().getOrDefault(owner);
            GiantFigureModel model = skin.playerSkin().model() == PlayerModelType.SLIM
                    ? slimPlayer : byOrnament.get(ornament);
            return new Look(model, skin.renderType());
        }

        /**
         * Steve or Alex, drawn solid or, with {@code seeThrough}, in a render type that can fade
         * (for the ownerless item's crossfade).
         */
        Look ownerless(boolean alex, boolean seeThrough) {
            GiantFigureModel model = alex ? slimPlayer : byOrnament.get(GiantOrnament.PLAYER);
            Identifier texture = alex ? ALEX : STEVE;
            return new Look(model, seeThrough
                    ? RenderTypes.entityTranslucent(texture) : model.renderType(texture));
        }
    }

    record Look(GiantFigureModel model, RenderType renderType) {}

    public static class State extends BlockEntityRenderState {
        GiantOrnament ornament = GiantOrnament.SKELETON;
        @Nullable ResolvableProfile owner;
        boolean alexWhenUnowned;
        Direction facing = Direction.NORTH;
        GiantFigureModel.Pose pose = GiantFigureModel.Pose.FORWARD;
    }

    private final Models models;

    public GiantOrnamentRenderer(BlockEntityRendererProvider.Context ctx) {
        this.models = Models.bake(ctx::bakeLayer);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GiantOrnamentBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.ornament = be.ornament();
        state.owner = be.owner();
        // An ornament placed without an owner (by command) is Steve or Alex, half and half by place.
        state.alexWhenUnowned = (Mth.getSeed(be.getBlockPos()) >>> 20 & 1) == 1;
        state.facing = be.facing();
        state.pose = new GiantFigureModel.Pose(be.headYaw(partialTick), be.headPitch(partialTick));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        submitFigure(models.look(state.ornament, state.owner, state.alexWhenUnowned), state.pose,
                state.facing.toYRot(), SCALE, poseStack, collector, state.lightCoords);
        poseStack.popPose();
    }

    /**
     * Draws the figure standing on the pose's origin, {@code scale} times life size, facing
     * {@code bodyYaw} (degrees, as an entity's: 0 is south), the way a living entity's renderer
     * places its model.
     */
    static void submitFigure(Look look, GiantFigureModel.Pose pose, float bodyYaw, float scale,
                             PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitFigure(look, pose, bodyYaw, scale, poseStack, collector, light, -1);
    }

    /** As above, tinted {@code color} (ARGB), whose alpha fades a see-through look. */
    static void submitFigure(Look look, GiantFigureModel.Pose pose, float bodyYaw, float scale,
                             PoseStack poseStack, SubmitNodeCollector collector, int light, int color) {
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180f - bodyYaw));
        poseStack.scale(scale, scale, scale);
        poseStack.scale(-1f, -1f, 1f);
        poseStack.translate(0f, -1.501f, 0f);
        collector.submitModel(look.model(), pose, poseStack, look.renderType(), light, OverlayTexture.NO_OVERLAY,
                color, null, 0);
        poseStack.popPose();
    }

    /** It stands four blocks tall: keep drawing it while its base block is off screen. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /**
     * The box the figure fits in. NeoForge culls a block entity's renderer by this box (its loader
     * renderer overrides {@code getRenderBoundingBox} with it), so the figure still draws when only
     * its head is on screen.
     */
    public static AABB renderBounds(GiantOrnamentBlockEntity ornament) {
        return new AABB(ornament.getBlockPos()).expandTowards(0, 2 * SCALE, 0).inflate(SCALE / 2);
    }
}
