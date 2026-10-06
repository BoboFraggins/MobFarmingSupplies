package net.bobofraggins.mobfarmingsupplies.glamping.present.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentEndermanHandler;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric equivalent of NeoForge's {@code FinalizeSpawnEvent} for the Present enderman easter egg.
 * Endermen don't override {@link Mob#finalizeSpawn}, so this injects at the tail of Mob's.
 */
@Mixin(Mob.class)
public abstract class EndermanFinalizeSpawnMixin {

    @Inject(
            method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;"
                    + "Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)"
                    + "Lnet/minecraft/world/entity/SpawnGroupData;",
            at = @At("TAIL"))
    private void mobfarmingsupplies$maybeGivePresent(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        if (!((Object) this instanceof Enderman enderman)) return;
        if (spawnReason == EntitySpawnReason.COMMAND
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.BUCKET
                || spawnReason == EntitySpawnReason.DISPENSER) return;

        PresentEndermanHandler.tryGivePresent(enderman, enderman.level().getRandom());
    }
}
