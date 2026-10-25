package net.bobofraggins.mobfarmingsupplies.incubator;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

/**
 * Draws the Incubator's lid ({@code block/incubator_lid}, the "lid" group of
 * {@code block/incubator}), swung up about its hinge at the back by
 * {@link IncubatorBlockEntity#lidAngle}, and what's in the nests: the test tube while there's a DNA
 * sample, and each row's egg while that row has one. The lid is translucent; the eggs and tube are
 * cutout, like the base (drawn in the cutout layer, so it shows through the lid on 1.21.1, where
 * block entities draw before translucent blocks).
 */
public class IncubatorRenderer extends StateBlockEntityRenderer<IncubatorBlockEntity, IncubatorRenderer.State> {

    public static final ResourceLocation LID_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_lid");
    public static final ResourceLocation TEST_TUBE_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_test_tube");

    /**
     * Each row's nest, in reading order as you face the incubator's front (which faces north in
     * the model, so north is nearest you and east is on your left): far left, far right, near
     * left, near right.
     */
    public static final List<ResourceLocation> EGG_MODEL_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_southeast"),
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_southwest"),
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_northeast"),
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_northwest"));

    // The hinge, in north-facing model space: the top of the back (south) edge.
    private static final float HINGE_Y = 8f / 16f;
    private static final float HINGE_Z = 16f / 16f;

    private static final Quaternionf FACING_QUAT = new Quaternionf();
    private static final Quaternionf LID_QUAT = new Quaternionf();

    @SuppressWarnings("unused")
    public IncubatorRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float lidAngleDeg;
        boolean testTube;
        final boolean[] eggs = new boolean[IncubatorBlockEntity.ROWS];
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IncubatorBlockEntity be, State state, float partialTick) {
        state.facing = be.getBlockState().getValue(IncubatorBlock.FACING);
        state.lidAngleDeg = be.lidAngle(partialTick);
        state.testTube = be.showsTestTube();
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) state.eggs[row] = be.showsEgg(row);
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel lid = ExtraBlockModels.get(LID_MODEL_ID);
        if (lid == null) return;
        poseStack.pushPose();
        // Turn to the block's facing about its centre (the model faces north), as the blockstate does.
        poseStack.translate(0.5, 0.5, 0.5);
        switch (state.facing) {
            case EAST  -> poseStack.mulPose(FACING_QUAT.rotationY(-(float) (Math.PI / 2)));
            case SOUTH -> poseStack.mulPose(FACING_QUAT.rotationY( (float)  Math.PI));
            case WEST  -> poseStack.mulPose(FACING_QUAT.rotationY( (float) (Math.PI / 2)));
            default    -> {} // NORTH: no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        // What's in the nests: the test tube for a DNA sample, an egg for each row with one.
        if (state.testTube) submitPart(TEST_TUBE_MODEL_ID, state, poseStack, buffers);
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) {
            if (state.eggs[row]) submitPart(EGG_MODEL_IDS.get(row), state, poseStack, buffers);
        }
        // Swing the lid up about its hinge: the front edge rises.
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.mulPose(LID_QUAT.rotationX(state.lidAngleDeg * Mth.DEG_TO_RAD));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);
        ExtraBlockModels.render(poseStack, buffers, lid, Sheets.translucentCullBlockSheet(), state.lightCoords,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void submitPart(ResourceLocation modelId, State state, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel part = ExtraBlockModels.get(modelId);
        if (part == null) return;
        ExtraBlockModels.render(poseStack, buffers, part, Sheets.cutoutBlockSheet(), state.lightCoords,
                OverlayTexture.NO_OVERLAY);
    }

    /** The open lid reaches above the block, so NeoForge must not cull it to one block. */
    public static AABB renderBounds(IncubatorBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 0.5, 0).inflate(0.5);
    }
}
