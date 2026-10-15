package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorEvents;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies a nearby Wither Inhibitor to a Wither ({@link WitherInhibitorEvents}). */
@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {

    @Shadow @Final private ServerBossEvent bossEvent;

    /** Hides the boss bar while inhibited, and shows it again once the Wither leaves the area. */
    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void mobfarmingsupplies$hideBossBar(CallbackInfo ci) {
        bossEvent.setVisible(!WitherInhibitorEvents.inhibited((WitherBoss) (Object) this));
    }

    /** No explosion when a summoned Wither finishes powering up. */
    @Redirect(method = "customServerAiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    @Nullable
    private Explosion mobfarmingsupplies$skipSummonExplosion(Level level, Entity source, double x, double y, double z,
                                                             float radius, boolean fire,
                                                             Level.ExplosionInteraction interaction) {
        if (WitherInhibitorEvents.inhibited((WitherBoss) (Object) this)) return null; // the caller drops it
        return level.explode(source, x, y, z, radius, fire, interaction);
    }

    /** No skulls, aimed or random. */
    @Inject(method = "performRangedAttack(IDDDZ)V", at = @At("HEAD"), cancellable = true)
    private void mobfarmingsupplies$noSkulls(int head, double x, double y, double z, boolean dangerous,
                                             CallbackInfo ci) {
        if (WitherInhibitorEvents.inhibited((WitherBoss) (Object) this)) ci.cancel();
    }
}
