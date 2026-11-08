package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.wardeninhibitor.WardenInhibitorEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies a nearby Warden Inhibitor to a Warden's sonic boom ({@link WardenInhibitorEvents}). */
@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin {

    /** An inhibited Warden doesn't start charging. */
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z",
            at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$noCharge(ServerLevel level, Warden warden, CallbackInfoReturnable<Boolean> cir) {
        if (WardenInhibitorEvents.inhibited(warden)) cir.setReturnValue(false);
    }

    /** A Warden that walks into the area mid-charge gives up before it fires. */
    @Inject(method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)Z",
            at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$abandonCharge(ServerLevel level, Warden warden, long gameTime,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (WardenInhibitorEvents.inhibited(warden)) cir.setReturnValue(false);
    }
}
