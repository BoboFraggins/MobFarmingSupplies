package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

/** Model drawing with one ARGB tint, as 1.21 takes it (1.20.1 takes four floats). */
public final class ModelRendering {

    private ModelRendering() {}

    public static void render(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int argb) {
        part.render(poseStack, buffer, light, overlay,
                Colors.red(argb), Colors.green(argb), Colors.blue(argb), Colors.alpha(argb));
    }

    public static void render(Model model, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int argb) {
        model.renderToBuffer(poseStack, buffer, light, overlay,
                Colors.red(argb), Colors.green(argb), Colors.blue(argb), Colors.alpha(argb));
    }
}
