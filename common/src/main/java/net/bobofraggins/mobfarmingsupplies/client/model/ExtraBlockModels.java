package net.bobofraggins.mobfarmingsupplies.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonRenderer;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorItemRenderer;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorRenderer;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentRenderer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketRenderer;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-platform access to "extra" block models — whole block models that are not tied
 * to any blockstate or item, baked alongside the normal model set and looked up by id
 * at render time.
 *
 * <p>NeoForge implementation: {@code ExtraBlockModelsImpl} ({@code ModelEvent.RegisterAdditional}).
 * Fabric implementation: {@code ExtraBlockModelsImpl} ({@code ModelLoadingPlugin.addModels}).
 * Both register {@link #IDS}.
 */
public final class ExtraBlockModels {

    /** Every extra model, registered with the loader's model loading. */
    public static final List<ResourceLocation> IDS = List.of(
            FanBlockEntityRenderer.TURBINE_MODEL_ID,
            MobHarvesterRenderer.LEFT_ARM_FRONT_MODEL_ID,
            MobHarvesterRenderer.LEFT_ARM_SIDE_MODEL_ID,
            MobHarvesterRenderer.LEFT_ARM_REAR_MODEL_ID,
            MobHarvesterRenderer.RIGHT_ARM_FRONT_MODEL_ID,
            MobHarvesterRenderer.RIGHT_ARM_SIDE_MODEL_ID,
            MobHarvesterRenderer.RIGHT_ARM_REAR_MODEL_ID,
            MobHarvesterRenderer.HEAD_MODEL_ID,
            PicnicBasketRenderer.BODY_MODEL_ID,
            PicnicBasketRenderer.LEFT_LID_MODEL_ID,
            PicnicBasketRenderer.RIGHT_LID_MODEL_ID,
            PresentRenderer.MODEL_ID,
            ToiletBlockEntityRenderer.LID_MODEL_ID,
            ExperiencePylonRenderer.PYLON_MODEL_ID,
            IncubatorItemRenderer.BASE_MODEL_ID,
            IncubatorRenderer.LID_MODEL_ID,
            IncubatorRenderer.TEST_TUBE_MODEL_ID,
            IncubatorRenderer.EGG_MODEL_IDS.get(0),
            IncubatorRenderer.EGG_MODEL_IDS.get(1),
            IncubatorRenderer.EGG_MODEL_IDS.get(2),
            IncubatorRenderer.EGG_MODEL_IDS.get(3));

    private ExtraBlockModels() {}

    /**
     * Returns the baked model for {@code modelId}, or {@code null} if it has not
     * been baked yet (e.g. queried before the first resource reload completes).
     */
    @ExpectPlatform
    @Nullable
    public static BakedModel get(ResourceLocation modelId) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Draws {@code model} at the pose's origin, untinted, as a block model would be. */
    public static void render(PoseStack poseStack, MultiBufferSource buffers, BakedModel model,
                              RenderType renderType, int light, int overlay) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), buffers.getBuffer(renderType), null, model, 1f, 1f, 1f, light, overlay);
    }
}
