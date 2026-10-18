package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The Giant Skeleton Lawn Ornament as an item: the same posed skeleton, one block tall, standing
 * in the item's block space so the item model's display transforms place it.
 */
public class GiantSkeletonOrnamentItemRenderer implements SpecialModelRenderer<Void> {

    /** A skeleton is two blocks tall; as an item it fits in one. */
    private static final float ITEM_SCALE = 0.5f;

    @Nullable private GiantSkeletonModel model;

    @Nullable
    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, ItemDisplayContext context, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay, boolean hasFoil) {
        if (model == null) {
            model = new GiantSkeletonModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SKELETON));
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        // Faces south, the item's front.
        GiantSkeletonOrnamentRenderer.submitSkeleton(model, GiantSkeletonModel.Pose.FORWARD, 0f, ITEM_SCALE,
                poseStack, buffers, packedLight);
        poseStack.popPose();
    }
}
