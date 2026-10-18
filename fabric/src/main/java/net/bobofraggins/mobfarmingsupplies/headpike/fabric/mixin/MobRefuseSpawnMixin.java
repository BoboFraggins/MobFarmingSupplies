package net.bobofraggins.mobfarmingsupplies.headpike.fabric.mixin;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikes;
import net.bobofraggins.mobfarmingsupplies.headpike.fabric.RefusedSpawn;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Asks the Head Pikes about every mob as it's set up for spawning, and marks the ones they refuse. */
@Mixin(Mob.class)
public abstract class MobRefuseSpawnMixin implements RefusedSpawn {

    @Unique
    private boolean mobfarmingsupplies$refused;

    @Override
    public boolean mobfarmingsupplies$isRefused() {
        return mobfarmingsupplies$refused;
    }

    @Inject(
            method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;"
                    + "Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)"
                    + "Lnet/minecraft/world/entity/SpawnGroupData;",
            at = @At("HEAD"))
    private void mobfarmingsupplies$askHeadPikes(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason reason, SpawnGroupData groupData,
                                                 CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        if (HeadPikes.refuses(level, self, reason, self.getX(), self.getY(), self.getZ())) mobfarmingsupplies$refused = true;
    }
}
