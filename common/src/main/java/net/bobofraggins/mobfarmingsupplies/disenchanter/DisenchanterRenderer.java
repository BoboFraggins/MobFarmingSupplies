package net.bobofraggins.mobfarmingsupplies.disenchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The XP Powered Disenchanter's floating book: vanilla's enchanting table renderer, drawing the
 * vanilla book animation the disenchanter keeps ({@link DisenchanterBlockEntity#book}), so it
 * floats, opens and turns towards nearby players just as an enchanting table's does.
 */
public class DisenchanterRenderer implements BlockEntityRenderer<DisenchanterBlockEntity, EnchantTableRenderState> {

    private final EnchantTableRenderer vanilla;

    public DisenchanterRenderer(BlockEntityRendererProvider.Context ctx) {
        this.vanilla = new EnchantTableRenderer(ctx);
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return vanilla.createRenderState();
    }

    @Override
    public void extractRenderState(DisenchanterBlockEntity be, EnchantTableRenderState state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        EnchantingTableBlockEntity book = be.book();
        if (book != null) vanilla.extractRenderState(book, state, partialTick, camera, crumbling);
    }

    @Override
    public void submit(EnchantTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        vanilla.submit(state, poseStack, collector, cameraState);
    }
}
