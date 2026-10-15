package net.bobofraggins.mobfarmingsupplies.shared.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * An item to draw, filled in by {@link ItemModelResolver#updateForTopItem} and drawn by
 * {@link #submit}. Mirrors the class of this name in later versions, so renderer bodies stay the
 * same across branches; here it just remembers the stack and draws it with the item renderer.
 */
public class ItemStackRenderState {
    private ItemStack stack = ItemStack.EMPTY;
    private ItemDisplayContext context = ItemDisplayContext.NONE;
    @Nullable private Level level;
    @Nullable private LivingEntity owner;
    private int seed;

    void set(ItemStack stack, ItemDisplayContext context, @Nullable Level level, @Nullable LivingEntity owner, int seed) {
        this.stack = stack;
        this.context = context;
        this.level = level;
        this.owner = owner;
        this.seed = seed;
    }

    public void clear() {
        stack = ItemStack.EMPTY;
        level = null;
        owner = null;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /** Draws the item at the pose's origin; {@code outlineColor} is ignored (no outlines here). */
    public void submit(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, int outlineColor) {
        if (stack.isEmpty()) return;
        Minecraft.getInstance().getItemRenderer().renderStatic(
                owner, stack, context, false, poseStack, buffers, level, light, overlay, seed);
    }
}
