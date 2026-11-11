package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.runestone.Runestones;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Damage a player's runestones make them immune to (fire, wither, wither skulls). Fire immunity
 * already stops fire damage; this also covers fire damage a mod deals without that check.
 * See {@link Runestones}.
 */
@Mixin(Player.class)
public abstract class RunestonePlayerMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$runestoneDamageImmune(ServerLevel level, DamageSource source,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (Runestones.blocksDamage((Player) (Object) this, source)) {
            cir.setReturnValue(true);
        }
    }
}
