package net.bobofraggins.mobfarmingsupplies.enderinhibitor.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric equivalent of {@code NeoForgeEnderInhibitorEvents}: cancels Enderman teleports
 * (both random/escape and toward-target) that originate near an active Ender Inhibitor.
 *
 * <p>NeoForge has {@code EntityTeleportEvent.EnderEntity} for this; Fabric has no
 * equivalent API, so this mixin injects into the private
 * {@code EnderMan#teleport(double, double, double)} method that both
 * {@code EnderMan#teleport()} and {@code EnderMan#teleportTowards(Entity)} funnel through.
 */
@Mixin(EnderMan.class)
public abstract class EnderManMixin {

    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$cancelTeleportNearInhibitor(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        EnderMan self = (EnderMan) (Object) this;
        if (EnderInhibitorEvents.inhibitorNearby(self)) {
            cir.setReturnValue(false);
        }
    }
}
