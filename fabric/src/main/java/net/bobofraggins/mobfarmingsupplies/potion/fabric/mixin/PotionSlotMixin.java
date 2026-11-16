package net.bobofraggins.mobfarmingsupplies.potion.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets essence bottles and our potions into the brewing stand window's bottle slots (vanilla takes only potions). */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class PotionSlotMixin {

    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void mobfarmingsupplies$mayPlaceItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MFSBrewing.isInput(stack)) cir.setReturnValue(true);
    }
}
