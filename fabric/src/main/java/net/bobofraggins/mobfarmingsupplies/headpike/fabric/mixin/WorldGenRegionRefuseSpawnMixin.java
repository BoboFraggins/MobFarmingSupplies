package net.bobofraggins.mobfarmingsupplies.headpike.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.headpike.fabric.RefusedSpawn;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps mobs a Head Pike refused out of the world while new chunks fill in. */
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionRefuseSpawnMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$keepOutRefused(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof RefusedSpawn refused && refused.mobfarmingsupplies$isRefused()) cir.setReturnValue(false);
    }
}
