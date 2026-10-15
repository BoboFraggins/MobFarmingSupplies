package net.bobofraggins.mobfarmingsupplies.shared.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/** Drawing helpers shaped like later versions' submit calls, so renderer bodies port unchanged. */
public final class Geometry {

    private Geometry() {}

    /** Runs {@code draw} with the current pose and a buffer of {@code renderType}. */
    public static void custom(PoseStack poseStack, MultiBufferSource buffers, RenderType renderType,
                              BiConsumer<PoseStack.Pose, VertexConsumer> draw) {
        draw.accept(poseStack.last(), buffers.getBuffer(renderType));
    }
}
