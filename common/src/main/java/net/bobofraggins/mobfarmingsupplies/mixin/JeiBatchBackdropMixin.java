package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.client.model.GuiBackdrop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link GuiBackdrop}'s glow in JEI's ingredient list too. JEI draws items with plain models in a
 * batch of its own rather than through {@code GuiGraphics.renderItem} (where
 * {@link GuiGraphicsBackdropMixin} adds it), so without this only items with custom renderers got
 * it there. Does nothing without JEI.
 */
@Pseudo
@Mixin(targets = "mezz.jei.library.render.batch.ItemStackBatchRenderer", remap = false)
public abstract class JeiBatchBackdropMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), require = 0, remap = false)
    private void mobfarmingsupplies$backdrop(GuiGraphics graphics, ItemRenderer renderer, BakedModel model,
                                             ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!stack.isEmpty() && GuiBackdrop.wants(stack)) GuiBackdrop.draw(graphics, x, y);
    }
}
