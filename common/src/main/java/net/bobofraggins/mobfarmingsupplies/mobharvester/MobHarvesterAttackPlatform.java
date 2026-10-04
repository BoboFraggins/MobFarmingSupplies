package net.bobofraggins.mobfarmingsupplies.mobharvester;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MobHarvesterAttackPlatform {

    private MobHarvesterAttackPlatform() {}

    /**
     * Attacks all targets in the kill zone using a fake player wielding the given sword, so
     * kills are player-attributed (Looting, XP, player-kill drops).
     *
     * <p>{@code reuse} is the fake player returned by the previous call for this harvester (or
     * {@code null}); it is reused when still valid, otherwise a new one is made. Returns the fake
     * player used, which the caller keeps for next time.
     */
    @ExpectPlatform
    public static ServerPlayer attackTargets(ServerLevel level, BlockPos pos, ItemStack sword,
                                             List<LivingEntity> targets, @Nullable ServerPlayer reuse) {
        throw new AssertionError("Missing platform implementation");
    }
}
