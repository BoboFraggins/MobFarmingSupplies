package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

/**
 * Draws a skull item as its head, for block entities that show skulls off their own block: our
 * Mob Heads, vanilla skulls (player heads with their skin) and, failing those, the item itself.
 * One per renderer: it caches the vanilla skull models.
 */
public final class SkullStackDrawer {

    private final Function<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    public SkullStackDrawer(EntityModelSet models) {
        this.skullModels = Util.memoize(type -> SkullBlockRenderer.createModel(models, type));
    }

    /**
     * Draws {@code stack}'s head standing on the pose's origin, centred, facing the way a vanilla
     * skull placed with rotation 0 does. {@code tint} (-1 = none) colours it; skulls from other
     * mods are drawn as their item, untinted.
     */
    public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int tint) {
        poseStack.pushPose();
        EntityType<?> mob = stack.get(Registration.MOB_HEAD_TYPE.get());
        if (mob != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            MobHeadModels.submit(mob, poseStack, collector, light, tint);
        } else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractSkullBlock block
                && skullModels.apply(block.getType()) != null) {
            poseStack.scale(-1f, -1f, 1f);
            collector.submitModel(skullModels.apply(block.getType()), new SkullModelBase.State(), poseStack,
                    renderType(block.getType(), stack), light, OverlayTexture.NO_OVERLAY, tint, null, 0, null);
        } else {
            Minecraft mc = Minecraft.getInstance();
            mc.getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.NONE, mc.level, null, 0);
            poseStack.translate(-0.5f, 0f, -0.5f);
            itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }

    private static RenderType renderType(SkullBlock.Type type, ItemStack stack) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (type == SkullBlock.Types.PLAYER && profile != null) {
            return Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile).renderType();
        }
        return SkullBlockRenderer.getSkullRenderType(type, null);
    }
}
