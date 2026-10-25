package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Moves experience between a player and an Experience Pylon, by levels as the Experience Syringe
 * does: one level stored is what's above the player's last whole level (or the whole level below,
 * when they're exactly on one), and one level retrieved is what takes them to their next whole
 * level. Points convert to fluid at the syringe's rate.
 */
public final class PylonExperience {

    /** Fluid per experience point, as the syringe counts it (a bucket is 50 points). */
    public static final long MB_PER_XP = 1000 / ExperienceSyringeItem.XP_PER_BUCKET;

    /** Store or retrieve this many levels; {@link #ALL} for everything. */
    public static final int ALL = -1;

    private PylonExperience() {}

    /** Stores {@code levels} of the player's experience (or all of it) in the pylon. */
    public static void store(ExperiencePylonBlockEntity pylon, ServerPlayer player, int levels) {
        int wanted = levels == ALL ? totalPoints(player) : pointsBelow(player, levels);
        if (wanted <= 0) return;
        FluidStack fluid = pylon.isLocked()
                ? pylon.getStoredFluid()
                : FluidStack.create(Registration.XP_JUICE_SOURCE.get(), 1);
        int points = (int) Math.min(wanted, pylon.insert(fluid, wanted * MB_PER_XP, true) / MB_PER_XP);
        if (points <= 0) return;
        pylon.insert(fluid, points * MB_PER_XP, false);
        if (points == totalPoints(player)) {
            player.setExperienceLevels(0);
            player.setExperiencePoints(0);
        } else {
            player.giveExperiencePoints(-points);
        }
        playSound(player);
    }

    /** Gives the player {@code levels} levels of experience (or everything) from the pylon. */
    public static void retrieve(ExperiencePylonBlockEntity pylon, ServerPlayer player, int levels) {
        long available = pylon.getAmount() / MB_PER_XP;
        long wanted = levels == ALL ? available : pointsAbove(player, levels);
        int points = (int) Math.min(Math.min(wanted, available), Integer.MAX_VALUE);
        if (points <= 0) return;
        pylon.extract(points * MB_PER_XP, false);
        player.giveExperiencePoints(points);
        playSound(player);
    }

    /** The whole levels, counted up from level 0, that {@code mb} of stored experience would give. */
    public static int levelsFor(long mb) {
        long points = mb / MB_PER_XP;
        int level = 0;
        while (points >= ExperienceSyringeItem.xpNeededForLevel(level)) {
            points -= ExperienceSyringeItem.xpNeededForLevel(level);
            level++;
        }
        return level;
    }

    /** "Stored XP: N Levels", for the UI and Jade. */
    public static Component storedLine(long mb) {
        return Component.translatable("gui.mobfarmingsupplies.experience_pylon.stored", levelsFor(mb));
    }

    /** All of a player's experience, in points. */
    static int totalPoints(ServerPlayer player) {
        long points = progressPoints(player);
        for (int level = 0; level < player.experienceLevel; level++) {
            points += ExperienceSyringeItem.xpNeededForLevel(level);
        }
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    /** Points that come off the player going down {@code levels} whole levels. */
    private static int pointsBelow(ServerPlayer player, int levels) {
        long points = 0;
        int level = player.experienceLevel;
        int progress = progressPoints(player);
        for (int i = 0; i < levels; i++) {
            if (progress > 0) {
                points += progress;
                progress = 0;
            } else if (level > 0) {
                level--;
                points += ExperienceSyringeItem.xpNeededForLevel(level);
            } else {
                break;
            }
        }
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    /** Points that take the player up {@code levels} whole levels. */
    private static long pointsAbove(ServerPlayer player, int levels) {
        long points = 0;
        int level = player.experienceLevel;
        int progress = progressPoints(player);
        for (int i = 0; i < levels; i++) {
            points += ExperienceSyringeItem.xpNeededForLevel(level) - progress;
            progress = 0;
            level++;
        }
        return points;
    }

    private static int progressPoints(ServerPlayer player) {
        return Mth.floor(player.experienceProgress * player.getXpNeededForNextLevel() + 0.5f);
    }

    private static void playSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}
