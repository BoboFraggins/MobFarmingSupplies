package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.muffler.MufflerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sounds from inside a Muffler's area aren't played (see {@link MufflerSounds}). Sounds with no
 * place in the world (music, menu clicks, anything relative to the listener) are left alone.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMufflerMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$muffle(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) return;
        if (MufflerSounds.muffles(Minecraft.getInstance().level, sound.getX(), sound.getY(), sound.getZ())) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
