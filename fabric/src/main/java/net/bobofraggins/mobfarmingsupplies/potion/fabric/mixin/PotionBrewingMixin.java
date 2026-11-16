package net.bobofraggins.mobfarmingsupplies.potion.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds our brewing recipes ({@link MFSBrewing}) to vanilla's. Vanilla's mixes only make vanilla
 * potions (a {@code Potion} in a potion item), and Fabric has no hook for anything else.
 */
@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$isIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (MFSBrewing.isIngredient(ingredient)) cir.setReturnValue(true);
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$hasMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!MFSBrewing.mix(input, ingredient).isEmpty()) cir.setReturnValue(true);
    }

    /** Note vanilla's argument order: the ingredient first, then the bottle. */
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$mix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = MFSBrewing.mix(input, ingredient);
        if (!output.isEmpty()) cir.setReturnValue(output);
    }
}
