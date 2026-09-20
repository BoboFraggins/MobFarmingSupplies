package net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the Magic Hat on the wearer's head when equipped in a Trinkets "head/hat" slot.
 *
 * <p>Mirrors {@code MagicHatCurioRenderer} (NeoForge/Curios) exactly — same pose math, same
 * item-model-resolver draw call — just against Trinkets Updated's {@link TrinketRenderer}
 * shape instead of Curios' {@code ICurioRenderer}.
 */
public final class MagicHatTrinketRenderer implements TrinketRenderer {

    @Override
    public void submit(
            ItemStack stack,
            TrinketSlotAccess slot,
            EntityModel<? extends LivingEntityRenderState> model,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            LivingEntityRenderState renderState,
            float yRotation,
            float xRotation) {

        if (stack.isEmpty()) return;
        if (!(model instanceof HumanoidModel<?> humanoid)) return;

        poseStack.pushPose();
        humanoid.head.translateAndRotate(poseStack);
        poseStack.rotate(Axis.YP.rotationDegrees(180f));
        poseStack.rotate(Axis.ZP.rotationDegrees(180f));
        poseStack.translate(0.0, 1.0, 0.0);

        ItemStackRenderState irs = new ItemStackRenderState();
        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(irs, stack, ItemDisplayContext.NONE, null, null, 0);
        irs.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
