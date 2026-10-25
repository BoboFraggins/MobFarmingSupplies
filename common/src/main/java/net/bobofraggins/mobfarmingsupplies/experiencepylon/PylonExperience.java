package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Moves experience between a player and an Experience Pylon, by levels as the Experience Syringe
 * does: one level stored is what's above the player's last whole level (or the whole level below,
 * when they're exactly on one), and one level retrieved is what takes them to their next whole
 * level. Points convert to fluid at the syringe's rate. The syringe moves one level at a time with
 * the same {@link #pointsBelow} and {@link #pointsAbove}.
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
        addPoints(player, -points);
        playSound(player);
    }

    /** Gives the player {@code levels} levels of experience (or everything) from the pylon. */
    public static void retrieve(ExperiencePylonBlockEntity pylon, ServerPlayer player, int levels) {
        long available = pylon.getAmount() / MB_PER_XP;
        long wanted = levels == ALL ? available : pointsAbove(player, levels);
        int points = (int) Math.min(Math.min(wanted, available), Integer.MAX_VALUE);
        if (points <= 0) {
            addPoints(player, 0); // a bar left a hair short of full by earlier XP becomes the next level
            return;
        }
        pylon.extract(points * MB_PER_XP, false);
        addPoints(player, points);
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

    /**
     * A player's experience as a whole level and the points into the next. Vanilla keeps the
     * points as a float fraction of the level, which adding points can leave a hair short of a
     * full bar without levelling up; a bar that rounds to full counts as the next level.
     */
    private record Experience(int level, int points) {}

    private static Experience experience(Player player) {
        int level = player.experienceLevel;
        int needed = ExperienceSyringeItem.xpNeededForLevel(level);
        int points = Math.max(0, Math.round(player.experienceProgress * needed));
        return points >= needed ? new Experience(level + 1, 0) : new Experience(level, points);
    }

    /**
     * Gives the player {@code delta} points (or takes them, when negative), working in whole
     * levels and setting the result exactly, so a level's worth always lands on the level.
     * Vanilla's {@code giveExperiencePoints} adds float fractions and can stop just short.
     */
    public static void addPoints(ServerPlayer player, int delta) {
        Experience start = experience(player);
        int level = start.level();
        long points = start.points();
        long left = Math.abs((long) delta);
        if (delta >= 0) {
            while (points + left >= ExperienceSyringeItem.xpNeededForLevel(level)) {
                left -= ExperienceSyringeItem.xpNeededForLevel(level) - points;
                points = 0;
                level++;
            }
            points += left;
        } else {
            while (left > points && level > 0) {
                left -= points;
                level--;
                points = ExperienceSyringeItem.xpNeededForLevel(level);
            }
            points = Math.max(0, points - left);
        }
        player.setExperienceLevels(level);
        player.setExperiencePoints((int) points);
        player.totalExperience = (int) Math.max(0, Math.min((long) player.totalExperience + delta, Integer.MAX_VALUE));
    }

    /** All of a player's experience, in points. */
    static int totalPoints(Player player) {
        Experience now = experience(player);
        long points = now.points();
        for (int level = 0; level < now.level(); level++) {
            points += ExperienceSyringeItem.xpNeededForLevel(level);
        }
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    /** Points that come off the player going down {@code levels} whole levels. */
    public static int pointsBelow(Player player, int levels) {
        long points = 0;
        Experience now = experience(player);
        int level = now.level();
        int progress = now.points();
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

    /**
     * Points that take the player up {@code levels} whole levels. A bar a hair short of full
     * counts as one of them: finishing it (for no points, see {@link #addPoints}) is a level up.
     */
    public static long pointsAbove(Player player, int levels) {
        long points = 0;
        Experience now = experience(player);
        int level = now.level();
        int progress = now.points();
        for (int i = now.level() > player.experienceLevel ? 1 : 0; i < levels; i++) {
            points += ExperienceSyringeItem.xpNeededForLevel(level) - progress;
            progress = 0;
            level++;
        }
        return points;
    }

    private static void playSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.5f, 1.0f);
    }
}
