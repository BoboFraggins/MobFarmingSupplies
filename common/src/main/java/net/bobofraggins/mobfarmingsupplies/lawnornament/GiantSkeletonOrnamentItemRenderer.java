package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * The Giant Skeleton Lawn Ornament as an item: the same posed skeleton, one block tall, standing
 * in the item's block space so the item model's display transforms place it.
 */
public class GiantSkeletonOrnamentItemRenderer implements SpecialModelRenderer<Void> {

    /** A skeleton is two blocks tall; as an item it fits in one. */
    private static final float ITEM_SCALE = 0.5f;

    @Nullable private GiantSkeletonModel model;

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Nullable
    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int packedLight, int packedOverlay, boolean hasFoil, int tint) {
        if (model == null) {
            model = new GiantSkeletonModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SKELETON));
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        // Faces south, the item's front.
        GiantSkeletonOrnamentRenderer.submitSkeleton(model, GiantSkeletonModel.Pose.FORWARD, 0f, ITEM_SCALE,
                poseStack, collector, packedLight);
        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new GiantSkeletonOrnamentItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }
    }
}
