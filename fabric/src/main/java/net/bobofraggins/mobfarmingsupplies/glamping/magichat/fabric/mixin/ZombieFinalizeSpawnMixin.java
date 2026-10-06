package net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatZombieHandler;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric equivalent of NeoForge's {@code FinalizeSpawnEvent} for the Magic Hat's zombie
 * easter egg — Fabric has no such spawn-finalize event, so this mixin injects at the tail
 * of {@link Zombie#finalizeSpawn}, after vanilla's own equipment/baby-chance logic has run
 * (matching where NeoForge's event fires).
 */
@Mixin(Zombie.class)
public abstract class ZombieFinalizeSpawnMixin {

    @Inject(
            method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;"
                    + "Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)"
                    + "Lnet/minecraft/world/entity/SpawnGroupData;",
            at = @At("TAIL"))
    private void mobfarmingsupplies$maybeEquipMagicHat(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        if (spawnReason == EntitySpawnReason.COMMAND
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.BUCKET
                || spawnReason == EntitySpawnReason.DISPENSER) return;

        Zombie self = (Zombie) (Object) this;
        MagicHatZombieHandler.tryEquipMagicHat(self, self.level().getRandom());
    }
}
