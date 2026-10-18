package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemModelResolver;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemStackRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

/**
 * Draws a skull item as its head, for block entities that show skulls off their own block: our
 * Mob Heads, vanilla skulls (player heads with their skin) and, failing those, the item itself.
 * One per renderer: it holds the vanilla skull models.
 */
public final class SkullStackDrawer {

    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    public SkullStackDrawer(EntityModelSet models) {
        this.skullModels = SkullBlockRenderer.createSkullRenderers(models);
    }

    /**
     * Draws {@code stack}'s head standing on the pose's origin, centred, facing the way a vanilla
     * skull placed with rotation 0 does. {@code tint} (-1 = none) colours it; skulls from other
     * mods are drawn as their item, untinted.
     */
    public void submit(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, int tint) {
        poseStack.pushPose();
        EntityType<?> mob = stack.get(Registration.MOB_HEAD_TYPE.get());
        SkullModelBase model = stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractSkullBlock block
                ? skullModels.get(block.getType()) : null;
        if (mob != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            MobHeadModels.submit(mob, poseStack, buffers, light, tint);
        } else if (model != null) {
            SkullBlock.Type type = ((AbstractSkullBlock) ((BlockItem) stack.getItem()).getBlock()).getType();
            poseStack.scale(-1f, -1f, 1f);
            model.setupAnim(0f, 0f, 0f);
            model.renderToBuffer(poseStack,
                    buffers.getBuffer(SkullBlockRenderer.getRenderType(type, stack.get(DataComponents.PROFILE))),
                    light, OverlayTexture.NO_OVERLAY, tint);
        } else {
            Minecraft mc = Minecraft.getInstance();
            ItemModelResolver.INSTANCE.updateForTopItem(itemState, stack, ItemDisplayContext.NONE, mc.level, null, 0);
            poseStack.translate(-0.5f, 0f, -0.5f);
            itemState.submit(poseStack, buffers, light, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }
}
