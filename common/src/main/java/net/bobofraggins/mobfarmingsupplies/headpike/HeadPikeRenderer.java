package net.bobofraggins.mobfarmingsupplies.headpike;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.mobhead.SkullStackDrawer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws the skulls on a Head Pike (the pike itself is its block model). They're two thirds the
 * size of a skull block, stuck on the pole from just under its iron tip down, a pixel apart. Each
 * sits a little askew: turned and tipped by its own amount, fixed by where the pike stands, so
 * they don't all line up.
 */
public class HeadPikeRenderer implements BlockEntityRenderer<HeadPikeBlockEntity, HeadPikeRenderer.State> {

    /** How big the skulls are next to a skull block. */
    private static final float SKULL_SCALE = 2f / 3f;
    /** A skull's height, in pixels (a skull block's is 8). */
    private static final float SKULL_HEIGHT = 8f * SKULL_SCALE;
    /** Where the top skull's base is, in pixels up from the bottom of the pike: low enough to show the iron tip. */
    private static final float TOP_SKULL_BASE = 28f - SKULL_HEIGHT;
    /** From one skull's base to the next one down, in pixels: a skull and a one-pixel gap. */
    private static final float SKULL_STEP = SKULL_HEIGHT + 1f;
    private static final float MAX_TURN = 35f;
    private static final float MAX_TIP = 9f;

    public static class State extends BlockEntityRenderState {
        final List<ItemStack> skulls = new ArrayList<>();
        float yaw;
        long seed;
        int skullLight;
    }

    private final SkullStackDrawer skulls;

    public HeadPikeRenderer(BlockEntityRendererProvider.Context ctx) {
        this.skulls = new SkullStackDrawer(ctx.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HeadPikeBlockEntity pike, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pike, state, crumbling);
        state.skulls.clear();
        state.skulls.addAll(pike.skulls());
        state.yaw = RotationSegment.convertToDegrees(pike.getBlockState().getValue(HeadPikeBlock.ROTATION));
        state.seed = Mth.getSeed(pike.getBlockPos());
        state.skullLight = pike.getLevel() == null ? state.lightCoords
                : LightCoordsUtil.getLightCoords(pike.getLevel(), pike.getBlockPos().above());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RandomSource askew = RandomSource.create(state.seed);
        for (int i = 0; i < state.skulls.size(); i++) {
            float turn = (askew.nextFloat() * 2 - 1) * MAX_TURN;
            float tipX = (askew.nextFloat() * 2 - 1) * MAX_TIP;
            float tipZ = (askew.nextFloat() * 2 - 1) * MAX_TIP;
            poseStack.pushPose();
            poseStack.translate(0.5f, (TOP_SKULL_BASE - i * SKULL_STEP) / 16f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-(state.yaw + turn)));
            // Tip it about its own centre, where the pole runs through it.
            float halfHeight = SKULL_HEIGHT / 2f / 16f;
            poseStack.translate(0f, halfHeight, 0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(tipX));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tipZ));
            poseStack.translate(0f, -halfHeight, 0f);
            poseStack.scale(SKULL_SCALE, SKULL_SCALE, SKULL_SCALE);
            skulls.submit(state.skulls.get(i), poseStack, collector, i == 0 ? state.skullLight : state.lightCoords, -1);
            poseStack.popPose();
        }
    }

    /**
     * The box the pike's skulls fit in: both its blocks. NeoForge culls a block entity's renderer
     * by this box (its loader renderer overrides {@code getRenderBoundingBox} with it), so the top
     * skulls still draw when the bottom block is off screen.
     */
    public static AABB renderBounds(HeadPikeBlockEntity pike) {
        return new AABB(pike.getBlockPos()).expandTowards(0, 1, 0);
    }
}
