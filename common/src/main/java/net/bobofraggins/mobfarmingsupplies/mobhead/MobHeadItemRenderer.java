package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * Item renderer for Mob Heads — in hand, in GUIs and worn on the head. Drawn in the same space as
 * vanilla's skull item renderer, so the item model's {@code template_skull} transforms fit it.
 */
public class MobHeadItemRenderer implements SpecialModelRenderer<EntityType<?>> {

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Nullable
    @Override
    public EntityType<?> extractArgument(ItemStack stack) {
        return stack.get(Registration.MOB_HEAD_TYPE.get());
    }

    @Override
    public void submit(@Nullable EntityType<?> type, PoseStack poseStack, SubmitNodeCollector collector,
                       int packedLight, int packedOverlay, boolean hasFoil, int tint) {
        if (type == null) return;
        poseStack.pushPose();
        // The same transform vanilla's head items use (translate, then 180 degrees about x).
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        MobHeadModels.submit(type, poseStack, collector, packedLight);
        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<EntityType<?>> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        @Nullable
        public SpecialModelRenderer<EntityType<?>> bake(SpecialModelRenderer.BakingContext context) {
            MobHeadModels.clear(); // models were just rebaked
            return new MobHeadItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<EntityType<?>>> type() {
            return MAP_CODEC;
        }
    }
}
