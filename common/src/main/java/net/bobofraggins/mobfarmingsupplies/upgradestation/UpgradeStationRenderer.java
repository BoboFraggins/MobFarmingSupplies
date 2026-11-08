package net.bobofraggins.mobfarmingsupplies.upgradestation;

import com.mojang.blaze3d.vertex.PoseStack;
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
 * Draws the Upgrade Station's translucent lid ({@code block/upgrade_station_lid}, the "lid" group
 * of {@code block/upgrade_station}), swung up about its hinge at the back by
 * {@link UpgradeStationBlockEntity#lidAngle}.
 */
public class UpgradeStationRenderer extends StateBlockEntityRenderer<UpgradeStationBlockEntity, UpgradeStationRenderer.State> {

    public static final ResourceLocation LID_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/upgrade_station_lid");

    // The hinge, in north-facing model space: the top of the back (south) edge.
    private static final float HINGE_Y = 13f / 16f;
    private static final float HINGE_Z = 16f / 16f;

    private static final Quaternionf FACING_QUAT = new Quaternionf();
    private static final Quaternionf LID_QUAT = new Quaternionf();

    @SuppressWarnings("unused")
    public UpgradeStationRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float lidAngleDeg;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(UpgradeStationBlockEntity be, State state, float partialTick) {
        state.facing = be.getBlockState().getValue(UpgradeStationBlock.FACING);
        state.lidAngleDeg = be.lidAngle(partialTick);
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
        // Swing the lid up about its hinge: the front edge rises.
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.mulPose(LID_QUAT.rotationX(state.lidAngleDeg * Mth.DEG_TO_RAD));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);
        ExtraBlockModels.render(poseStack, buffers, lid, Sheets.translucentCullBlockSheet(), state.lightCoords,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /** The open lid reaches above the block, so NeoForge must not cull it to one block. */
    public static AABB renderBounds(UpgradeStationBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0).inflate(0.25);
    }
}
