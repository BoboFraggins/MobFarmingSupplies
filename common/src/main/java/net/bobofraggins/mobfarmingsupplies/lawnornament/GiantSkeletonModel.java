package net.bobofraggins.mobfarmingsupplies.lawnornament;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Vanilla's skeleton model, posed for the lawn ornament: standing, arms at its sides, head turned
 * by {@link Pose}. Posed when drawn, so every ornament keeps its own head.
 */
public class GiantSkeletonModel extends Model<GiantSkeletonModel.Pose> {

    public static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    /** Where the head looks, relative to the body, in degrees (positive pitch looks down). */
    public record Pose(float headYawDeg, float headPitchDeg) {
        public static final Pose FORWARD = new Pose(0f, 0f);
    }

    private final ModelPart head;

    public GiantSkeletonModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
    }

    @Override
    public void setupAnim(Pose pose) {
        resetPose();
        head.yRot = pose.headYawDeg() * Mth.DEG_TO_RAD;
        head.xRot = pose.headPitchDeg() * Mth.DEG_TO_RAD;
        if (root.hasChild("hat")) {
            ModelPart hat = root.getChild("hat");
            hat.yRot = head.yRot;
            hat.xRot = head.xRot;
        }
    }
}
