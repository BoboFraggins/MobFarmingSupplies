package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.runestone.Runestone;
import net.bobofraggins.mobfarmingsupplies.runestone.Runestones;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A player wearing a Runestone of Fire Protection is fire-immune, like a Blaze: they never catch
 * fire (on either side, so no flames on screen) and take no fire damage. See {@link Runestones}.
 */
@Mixin(Entity.class)
public abstract class RunestoneEntityMixin {

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$runestoneFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Runestones.has(player, Runestone.FIRE_PROTECTION)) {
            cir.setReturnValue(true);
        }
    }
}
