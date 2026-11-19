package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Endermen covered by an Ender Inhibitor don't pick up blocks. The goal doesn't override
 * {@code canContinueToUse}, which falls back to {@code canUse}, so this stops it mid-way too.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class EnderManTakeBlockGoalMixin {

    @Shadow @Final private EnderMan enderman;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$inhibited(CallbackInfoReturnable<Boolean> cir) {
        if (EnderInhibitorEvents.inhibitorNearby(enderman)) cir.setReturnValue(false);
    }
}
