package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A vanilla humanoid model (a skeleton's, a player's, ...) posed for a giant lawn ornament:
 * standing, arms at its sides, head turned by {@link Pose}. Posed when drawn, so every ornament
 * keeps its own head.
 */
public class GiantFigureModel extends Model {

    /** Where the head looks, relative to the body, in degrees (positive pitch looks down). */
    public record Pose(float headYawDeg, float headPitchDeg) {
        public static final Pose FORWARD = new Pose(0f, 0f);
    }

    private final ModelPart root;
    private final ModelPart head;
    private final ResourceLocation texture;

    public GiantFigureModel(ModelPart root, ResourceLocation texture) {
        super(RenderType::entityCutout);
        this.root = root;
        this.head = root.getChild("head");
        this.texture = texture;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public void setupAnim(Pose pose) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.yRot = pose.headYawDeg() * Mth.DEG_TO_RAD;
        head.xRot = pose.headPitchDeg() * Mth.DEG_TO_RAD;
        if (root.hasChild("hat")) {
            ModelPart hat = root.getChild("hat");
            hat.yRot = head.yRot;
            hat.xRot = head.xRot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poseStack, buffer, light, overlay, color);
    }
}
