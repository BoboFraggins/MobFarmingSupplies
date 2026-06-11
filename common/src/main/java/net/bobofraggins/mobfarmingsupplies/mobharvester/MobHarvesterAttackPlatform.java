package net.bobofraggins.mobfarmingsupplies.mobharvester;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class MobHarvesterAttackPlatform {

    private MobHarvesterAttackPlatform() {}

    /**
     * Attacks all targets in the kill zone using a fake player wielding the given sword.
     * NeoForge impl uses HarvesterFakePlayer so kills are player-attributed.
     * Fabric impl falls back to direct LivingEntity.hurt().
     */
    @ExpectPlatform
    public static void attackTargets(ServerLevel level, BlockPos pos, ItemStack sword, List<LivingEntity> targets) {
        throw new AssertionError("Missing platform implementation");
    }
}
