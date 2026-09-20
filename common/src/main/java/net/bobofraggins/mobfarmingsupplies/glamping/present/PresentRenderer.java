package net.bobofraggins.mobfarmingsupplies.glamping.present;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Block-entity renderer for the Present — a single standalone model (loaded via
 * {@link ExtraBlockModels}, matching {@code PicnicBasketRenderer}'s pattern) yawed to match the
 * block's {@link PresentBlock#FACING}. The block's own blockstate model is an empty
 * particle-only placeholder ({@code block/present_particle.json}); all real geometry is drawn
 * here so the same one authored model can be reused for all four facings without four baked
 * blockstate variants.
 */
public class PresentRenderer implements BlockEntityRenderer<PresentBlockEntity, PresentRenderer.State> {

    public static final Identifier MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/present");

    private static final Quaternionf YAW_QUAT = new Quaternionf();

    public static class State extends BlockEntityRenderState {
        float facingYRotDeg;
    }

    @SuppressWarnings("unused")
    public PresentRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    private static float facingYRotDeg(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 0f;
        };
    }

    @Override
    public void extractRenderState(
            PresentBlockEntity be,
            State state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.hasProperty(PresentBlock.FACING)
                ? blockState.getValue(PresentBlock.FACING)
                : Direction.NORTH;
        state.facingYRotDeg = facingYRotDeg(facing);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        BlockStateModelPart model = ExtraBlockModels.get(MODEL_ID);
        if (model == null) return;

        float yawRad = state.facingYRotDeg * Mth.DEG_TO_RAD;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.rotate(YAW_QUAT.rotationY(yawRad));
        poseStack.translate(-0.5, -0.5, -0.5);
        collector.submitBlockModel(
                poseStack,
                Sheets.cutoutBlockItemSheet(),
                List.of(model),
                new int[0],
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);
        poseStack.popPose();
    }
}
