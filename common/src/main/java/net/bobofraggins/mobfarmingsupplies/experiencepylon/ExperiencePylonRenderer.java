package net.bobofraggins.mobfarmingsupplies.experiencepylon;

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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the pylon ({@code block/experience_pylon_pylon}, the "pylon" group of
 * {@code block/experience_pylon}) above the base, bobbing a pixel up and a pixel down over
 * {@link #BOB_PERIOD_TICKS}. Each pylon starts at its own point in the cycle, so a row of them
 * doesn't move in lockstep.
 */
public class ExperiencePylonRenderer
        implements BlockEntityRenderer<ExperiencePylonBlockEntity, ExperiencePylonRenderer.PylonState> {

    public static final Identifier PYLON_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/experience_pylon_pylon");

    private static final float BOB_PERIOD_TICKS = 200f; // 10 seconds
    private static final float BOB_HEIGHT = 1f / 16f;   // one pixel each way

    @SuppressWarnings("unused")
    public ExperiencePylonRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class PylonState extends BlockEntityRenderState {
        float bob;
    }

    @Override
    public PylonState createRenderState() {
        return new PylonState();
    }

    @Override
    public void extractRenderState(
            ExperiencePylonBlockEntity be,
            PylonState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        long time = be.getLevel() == null ? 0 : be.getLevel().getGameTime();
        float phase = Math.floorMod(be.getBlockPos().hashCode(), (int) BOB_PERIOD_TICKS);
        float cycle = ((time % (long) BOB_PERIOD_TICKS) + partialTick + phase) / BOB_PERIOD_TICKS;
        state.bob = Mth.sin(cycle * Mth.TWO_PI) * BOB_HEIGHT;
    }

    @Override
    public void submit(
            PylonState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        BlockStateModelPart pylon = ExtraBlockModels.get(PYLON_MODEL_ID);
        if (pylon == null) return;
        poseStack.pushPose();
        poseStack.translate(0, state.bob, 0);
        collector.submitBlockModel(
                poseStack,
                Sheets.translucentBlockSheet(),
                List.of(pylon),
                new int[0],
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);
        poseStack.popPose();
    }

    /** The pylon reaches most of a block above its own, so NeoForge must not cull it to one block. */
    public static AABB renderBounds(ExperiencePylonBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
    }
}
