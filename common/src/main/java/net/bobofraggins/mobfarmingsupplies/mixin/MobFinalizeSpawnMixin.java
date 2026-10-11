package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.tritanium.TritaniumMobEquipment;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runs {@link TritaniumMobEquipment} after the spawn setup of every mob that can spawn holding a
 * sword, axe or shovel. Each of these overrides {@code finalizeSpawn} and hands out its weapon
 * there; some (a Drowned, a Wither Skeleton) run their parent's too, which the depth count in
 * {@link TritaniumMobEquipment} accounts for.
 */
@Mixin({Zombie.class, Drowned.class, AbstractSkeleton.class, WitherSkeleton.class, Piglin.class,
        PiglinBrute.class, Vindicator.class, Vex.class})
public abstract class MobFinalizeSpawnMixin {

    private static final String FINALIZE_SPAWN = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;"
            + "Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;"
            + "Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;";

    @Inject(method = FINALIZE_SPAWN, at = @At("HEAD"))
    private void mobfarmingsupplies$enterSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        TritaniumMobEquipment.enterSpawn();
    }

    @Inject(method = FINALIZE_SPAWN, at = @At("RETURN"))
    private void mobfarmingsupplies$exitSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        TritaniumMobEquipment.exitSpawn((Mob) (Object) this, level, difficulty, reason);
    }
}
