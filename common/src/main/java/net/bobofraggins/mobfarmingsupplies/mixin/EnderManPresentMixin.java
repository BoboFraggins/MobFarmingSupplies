package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentBlock;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla never despawns an enderman carrying a block. One carrying a Present (5% of natural
 * spawns, see {@code PresentEndermanHandler}) may despawn like any other, so they don't pile up.
 */
@Mixin(EnderMan.class)
public abstract class EnderManPresentMixin {

    @Inject(method = "requiresCustomPersistence", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$presentCarrierMayDespawn(CallbackInfoReturnable<Boolean> cir) {
        EnderMan self = (EnderMan) (Object) this;
        BlockState carried = self.getCarriedBlock();
        // Mob#requiresCustomPersistence: only a mob riding something is kept.
        if (carried != null && carried.getBlock() instanceof PresentBlock) cir.setReturnValue(self.isPassenger());
    }
}
