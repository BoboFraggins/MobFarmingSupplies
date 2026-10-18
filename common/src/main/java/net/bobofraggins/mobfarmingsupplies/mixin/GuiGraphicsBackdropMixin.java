package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.client.model.GuiBackdrop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Item extras in every GUI that draws items (inventories, the hotbar, JEI), which later versions
 * get from the items' model definitions: {@link GuiBackdrop}'s glow behind the items that want
 * it, and a slot-sized clip for items drawn bigger than their slot (the lawn ornament's portrait).
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsBackdropMixin {

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("HEAD"))
    private void mobfarmingsupplies$beforeItem(LivingEntity entity, Level level, ItemStack stack, int x, int y,
                                                int seed, int guiOffset, CallbackInfo ci) {
        if (stack.isEmpty()) return;
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        if (GuiBackdrop.wants(stack)) GuiBackdrop.draw(graphics, x, y);
        if (GuiBackdrop.clipped(stack)) {
            // Scissor boxes are in screen space; the slot may be drawn under a translated pose.
            Vector3f from = graphics.pose().last().pose().transformPosition(x, y, 0, new Vector3f());
            Vector3f to = graphics.pose().last().pose().transformPosition(x + 16, y + 16, 0, new Vector3f());
            graphics.enableScissor((int) from.x, (int) from.y, (int) to.x, (int) to.y);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("RETURN"))
    private void mobfarmingsupplies$afterItem(LivingEntity entity, Level level, ItemStack stack, int x, int y,
                                               int seed, int guiOffset, CallbackInfo ci) {
        if (!stack.isEmpty() && GuiBackdrop.clipped(stack)) ((GuiGraphics) (Object) this).disableScissor();
    }
}
