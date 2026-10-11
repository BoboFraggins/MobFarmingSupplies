package net.bobofraggins.mobfarmingsupplies.brain;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Zombie Brains lying in XP Juice, source or flowing, come clean: after {@link #SOAK_TICKS} in it
 * (bubbling the while) the stack turns into Brains. Leaving the juice starts the soak over. The
 * juice isn't used up. Called each tick for every item entity (see {@code ItemEntitySoakMixin}).
 */
public final class BrainSoaking {

    public static final int SOAK_TICKS = 100;
    /** XP-orb green, for the bubbles. */
    private static final DustParticleOptions BUBBLE = new DustParticleOptions(0xB8F23A, 1.0f);

    private BrainSoaking() {}

    /** Advances {@code item}'s soak; returns the new soak time (0 when it isn't soaking). */
    public static int tick(ItemEntity item, int soaked) {
        if (!(item.level() instanceof ServerLevel level)) return 0;
        ItemStack stack = item.getItem();
        if (!stack.is(Registration.ZOMBIE_BRAIN.get()) || !inXpJuice(item)) return 0;

        if (++soaked % 5 == 0) {
            level.sendParticles(BUBBLE, item.getX(), item.getY() + 0.25, item.getZ(), 2, 0.15, 0.1, 0.15, 0);
        }
        if (soaked < SOAK_TICKS) return soaked;

        item.setItem(new ItemStack(Registration.BRAIN.get(), stack.getCount()));
        level.playSound(null, item.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.4f, 1.6f);
        level.sendParticles(BUBBLE, item.getX(), item.getY() + 0.25, item.getZ(), 12, 0.25, 0.2, 0.25, 0);
        return 0;
    }

    private static boolean inXpJuice(ItemEntity item) {
        AABB box = item.getBoundingBox().deflate(0.001);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (item.level().getFluidState(pos).getType().isSame(Registration.XP_JUICE_SOURCE.get())) return true;
        }
        return false;
    }
}
