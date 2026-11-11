package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.runestone.Runestone;
import net.bobofraggins.mobfarmingsupplies.runestone.Runestones;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runestones of Poison and Wither Protection make their wearer immune to those effects, and a
 * Runestone of Water Breathing lets them breathe underwater. See {@link Runestones}.
 */
@Mixin(LivingEntity.class)
public abstract class RunestoneLivingEntityMixin {

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$runestoneEffectImmune(MobEffectInstance effect,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Runestones.blocksEffect(player, effect.getEffect())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBreatheUnderwater", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$runestoneWaterBreathing(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Runestones.has(player, Runestone.WATER_BREATHING)) {
            cir.setReturnValue(true);
        }
    }
}
