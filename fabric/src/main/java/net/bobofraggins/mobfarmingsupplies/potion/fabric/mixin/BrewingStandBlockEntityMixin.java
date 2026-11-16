package net.bobofraggins.mobfarmingsupplies.potion.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets hoppers put essence bottles and our potions in an empty bottle slot (0-2), as they do potions. */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot < 3 && MFSBrewing.isInput(stack)) {
            cir.setReturnValue(((BrewingStandBlockEntity) (Object) this).getItem(slot).isEmpty());
        }
    }
}
