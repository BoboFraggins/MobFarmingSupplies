package net.bobofraggins.mobfarmingsupplies.incubator;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Draws the Incubator's lid ({@code block/incubator_lid}, the "lid" group of
 * {@code block/incubator}), swung up about its hinge at the back by
 * {@link IncubatorBlockEntity#lidAngle}, and what's in the nests: the test tube while there's a DNA
 * sample, and each row's egg while that row has one. Translucent, like the base.
 */
public class IncubatorRenderer implements BlockEntityRenderer<IncubatorBlockEntity, IncubatorRenderer.State> {

    public static final Identifier LID_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_lid");
    public static final Identifier TEST_TUBE_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_test_tube");

    /**
     * Each row's nest, in reading order as you face the incubator's front (which faces north in
     * the model, so north is nearest you and east is on your left): far left, far right, near
     * left, near right.
     */
    public static final List<Identifier> EGG_MODEL_IDS = List.of(
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_southeast"),
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_southwest"),
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_northeast"),
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_egg_northwest"));

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
    public void extractRenderState(IncubatorBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.facing = be.getBlockState().getValue(IncubatorBlock.FACING);
        state.lidAngleDeg = be.lidAngle(partialTick);
        state.testTube = be.showsTestTube();
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) state.eggs[row] = be.showsEgg(row);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        BlockStateModelPart lid = ExtraBlockModels.get(LID_MODEL_ID);
        if (lid == null) return;
        poseStack.pushPose();
        // Turn to the block's facing about its centre (the model faces north), as the blockstate does.
        poseStack.translate(0.5, 0.5, 0.5);
        switch (state.facing) {
            case EAST  -> poseStack.rotate(FACING_QUAT.rotationY(-(float) (Math.PI / 2)));
            case SOUTH -> poseStack.rotate(FACING_QUAT.rotationY( (float)  Math.PI));
            case WEST  -> poseStack.rotate(FACING_QUAT.rotationY( (float) (Math.PI / 2)));
            default    -> {} // NORTH: no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        // What's in the nests: the test tube for a DNA sample, an egg for each row with one.
        if (state.testTube) submitPart(TEST_TUBE_MODEL_ID, state, poseStack, collector);
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) {
            if (state.eggs[row]) submitPart(EGG_MODEL_IDS.get(row), state, poseStack, collector);
        }
        // Swing the lid up about its hinge: the front edge rises.
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.rotate(LID_QUAT.rotationX(state.lidAngleDeg * Mth.DEG_TO_RAD));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);
        collector.submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), List.of(lid), new int[0],
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static void submitPart(Identifier modelId, State state, PoseStack poseStack, SubmitNodeCollector collector) {
        BlockStateModelPart part = ExtraBlockModels.get(modelId);
        if (part == null) return;
        collector.submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), List.of(part), new int[0],
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    /** The open lid reaches above the block, so NeoForge must not cull it to one block. */
    public static AABB renderBounds(IncubatorBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 0.5, 0).inflate(0.5);
    }
}
