package net.bobofraggins.mobfarmingsupplies.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.bobofraggins.mobfarmingsupplies.wardeninhibitor.WardenInhibitorEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Applies a nearby Warden Inhibitor to a Warden's Darkness ({@link WardenInhibitorEvents}). */
@Mixin(Warden.class)
public abstract class WardenMixin {

    /** Wrapped at the Warden's own call, so Sculk Shriekers, which share the method, keep theirs. */
    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/warden/Warden;applyDarknessAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;I)V"))
    private void mobfarmingsupplies$noDarkness(ServerLevel level, Vec3 center, Entity source, int radius,
                                               Operation<Void> original) {
        if (!WardenInhibitorEvents.inhibited((Warden) (Object) this)) {
            original.call(level, center, source, radius);
        }
    }
}
