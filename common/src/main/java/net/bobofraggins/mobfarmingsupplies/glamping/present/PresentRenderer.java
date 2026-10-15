package net.bobofraggins.mobfarmingsupplies.glamping.present;

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
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

/**
 * Block-entity renderer for the Present — a single standalone model (loaded via
 * {@link ExtraBlockModels}, matching {@code PicnicBasketRenderer}'s pattern) yawed to match the
 * block's {@link PresentBlock#FACING}. The block's own blockstate model is an empty
 * particle-only placeholder ({@code block/present_particle.json}); all real geometry is drawn
 * here so the same one authored model can be reused for all four facings without four baked
 * blockstate variants.
 */
public class PresentRenderer extends StateBlockEntityRenderer<PresentBlockEntity, PresentRenderer.State> {

    public static final ResourceLocation MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/present");

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
    public void extractRenderState(PresentBlockEntity be, State state, float partialTick) {
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.hasProperty(PresentBlock.FACING)
                ? blockState.getValue(PresentBlock.FACING)
                : Direction.NORTH;
        state.facingYRotDeg = facingYRotDeg(facing);
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel model = ExtraBlockModels.get(MODEL_ID);
        if (model == null) return;

        float yawRad = state.facingYRotDeg * Mth.DEG_TO_RAD;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
        poseStack.translate(-0.5, -0.5, -0.5);
        ExtraBlockModels.render(poseStack, buffers, model, Sheets.cutoutBlockSheet(), state.lightCoords, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
