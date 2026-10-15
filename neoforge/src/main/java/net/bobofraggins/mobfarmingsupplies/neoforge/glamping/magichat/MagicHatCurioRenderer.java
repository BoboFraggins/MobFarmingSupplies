package net.bobofraggins.mobfarmingsupplies.neoforge.glamping.magichat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemModelResolver;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemStackRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Renders the Magic Hat on the wearer's head when equipped in a Curios "head" slot. */
public class MagicHatCurioRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffers,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        if (stack.isEmpty()) return;
        M model = renderLayerParent.getModel();
        if (!(model instanceof HumanoidModel<?> humanoid)) return;

        poseStack.pushPose();
        humanoid.head.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.translate(0.0, 1.0, 0.0);

        ItemStackRenderState irs = new ItemStackRenderState();
        ItemModelResolver.INSTANCE.updateForTopItem(irs, stack, ItemDisplayContext.NONE, null, null, 0);
        irs.submit(poseStack, buffers, light, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
