package net.bobofraggins.mobfarmingsupplies.headpike.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.headpike.fabric.RefusedSpawn;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps mobs a Head Pike refused out of the world. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelRefuseSpawnMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$keepOutRefused(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof RefusedSpawn refused && refused.mobfarmingsupplies$isRefused()) cir.setReturnValue(false);
    }
}
