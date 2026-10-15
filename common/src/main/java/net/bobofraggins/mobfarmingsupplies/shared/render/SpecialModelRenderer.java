package net.bobofraggins.mobfarmingsupplies.shared.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Draws an item in code rather than from its model, mirroring the interface of this name in later
 * versions so renderer bodies stay the same across branches. Registered per item in
 * {@link SpecialItemRenderers}; the item's model is {@code builtin/entity}, whose display
 * transforms place the renderer's 0-1 block space.
 */
public interface SpecialModelRenderer<T> {

    /** What {@link #submit} needs from the stack. */
    @Nullable
    T extractArgument(ItemStack stack);

    void submit(@Nullable T argument, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers,
                int packedLight, int packedOverlay, boolean hasFoil);

    /** Extracts and draws {@code stack}. */
    default void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers,
                        int packedLight, int packedOverlay) {
        submit(extractArgument(stack), context, poseStack, buffers, packedLight, packedOverlay, stack.hasFoil());
    }
}
