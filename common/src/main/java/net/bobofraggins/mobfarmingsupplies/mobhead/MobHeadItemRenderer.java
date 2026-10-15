package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item renderer for Mob Heads — in hand, in GUIs and worn on the head. Drawn in the same space as
 * vanilla's skull item renderer, so the item model's {@code template_skull} transforms fit it.
 */
public class MobHeadItemRenderer implements SpecialModelRenderer<EntityType<?>> {

    @Nullable
    @Override
    public EntityType<?> extractArgument(ItemStack stack) {
        return stack.get(Registration.MOB_HEAD_TYPE.get());
    }

    @Override
    public void submit(@Nullable EntityType<?> type, ItemDisplayContext context, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay, boolean hasFoil) {
        if (type == null) return;
        poseStack.pushPose();
        // The same transform vanilla's head items use (translate, then 180 degrees about x).
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        MobHeadModels.submit(type, poseStack, buffers, packedLight);
        poseStack.popPose();
    }
}
