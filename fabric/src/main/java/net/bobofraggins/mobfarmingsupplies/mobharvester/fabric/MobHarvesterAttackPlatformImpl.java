package net.bobofraggins.mobfarmingsupplies.mobharvester.fabric;

import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterAttackPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MobHarvesterAttackPlatformImpl {

    @Nullable
    public static ServerPlayer attackTargets(ServerLevel level, BlockPos pos, ItemStack sword,
                                             List<LivingEntity> targets, @Nullable ServerPlayer reuse) {
        // A fake player isn't in the world, so if something (Thorns, say) killed it, it would
        // silently stop attacking - make a fresh one rather than reuse a dead one.
        FabricHarvesterFakePlayer fp = reuse instanceof FabricHarvesterFakePlayer cached && cached.level() == level && cached.isAlive()
                ? cached
                : create(level);
        if (fp == null) {
            MobHarvesterAttackPlatform.hurtWithoutPlayer(level, sword, targets);
            return null;
        }
        fp.setPos(pos.getX() + 0.5, pos.getY() - 100.0, pos.getZ() + 0.5);
        fp.setItemInHand(InteractionHand.MAIN_HAND, sword.copy());
        MobHarvesterAttackPlatform.applySwordAttributes(fp, sword);
        for (LivingEntity target : targets) {
            fp.resetAttackStrength();
            fp.attack(target);
        }
        return fp;
    }

    @Nullable
    private static FabricHarvesterFakePlayer create(ServerLevel level) {
        if (MobHarvesterAttackPlatform.isFakePlayerUnavailable()) return null;
        try {
            return new FabricHarvesterFakePlayer(level);
        } catch (RuntimeException e) {
            MobHarvesterAttackPlatform.fakePlayerFailed(e);
            return null;
        }
    }
}
