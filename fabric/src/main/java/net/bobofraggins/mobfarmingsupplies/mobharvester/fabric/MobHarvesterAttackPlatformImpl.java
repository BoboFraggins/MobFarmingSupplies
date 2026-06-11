package net.bobofraggins.mobfarmingsupplies.mobharvester.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class MobHarvesterAttackPlatformImpl {

    public static void attackTargets(ServerLevel level, BlockPos pos, ItemStack sword, List<LivingEntity> targets) {
        FabricHarvesterFakePlayer fp = new FabricHarvesterFakePlayer(level);
        fp.setPos(pos.getX() + 0.5, pos.getY() - 100.0, pos.getZ() + 0.5);
        fp.setItemInHand(InteractionHand.MAIN_HAND, sword.copy());
        for (LivingEntity target : targets) {
            fp.resetAttackStrength();
            fp.attack(target);
        }
    }
}
