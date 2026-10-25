package net.bobofraggins.mobfarmingsupplies.blockbreaker;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemModelResolver;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemStackRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

/**
 * Draws the iron pickaxe turning slowly inside the Block Breaker's glass core. The core itself is
 * part of the block model; it's translucent, so it draws after this and tints the pickaxe.
 */
public class BlockBreakerRenderer extends StateBlockEntityRenderer<BlockBreakerBlockEntity, BlockBreakerRenderer.State> {

    /**
     * An item drawn for an item frame ({@link ItemDisplayContext#FIXED}) is a block across; this
     * fits the pickaxe just inside the 6 pixel glass core.
     */
    private static final float SCALE = 0.34f;

    private static final Quaternionf SPIN = new Quaternionf();

    private final ItemModelResolver itemModelResolver = ItemModelResolver.INSTANCE;
    // Made on first use: item components may not be bound yet when the renderer class loads.
    private ItemStack pickaxe;

    @SuppressWarnings("unused")
    public BlockBreakerRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class State extends BlockEntityRenderState {
        float angleDeg;
        final ItemStackRenderState item = new ItemStackRenderState();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlockBreakerBlockEntity be, State state, float partialTick) {
        // The same slow turn, by the computer's clock, as the inhibitors' floating items.
        state.angleDeg = EnderInhibitorBlockEntityRenderer.spinDegrees();
        if (pickaxe == null) pickaxe = new ItemStack(Items.IRON_PICKAXE);
        state.item.clear();
        itemModelResolver.updateForTopItem(state.item, pickaxe, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        if (state.item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(SPIN.rotationY(state.angleDeg * Mth.DEG_TO_RAD));
        poseStack.scale(SCALE, SCALE, SCALE);
        state.item.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
