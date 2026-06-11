package net.bobofraggins.mobfarmingsupplies.experiencesyringe;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Experience Syringe — stores XP as a fluid and transfers it to/from the player.
 *
 * <ul>
 *   <li><b>Right-click</b> — withdraws enough XP from the syringe to complete the
 *       player's current level (i.e. reaches the next whole level boundary).</li>
 *   <li><b>Shift + right-click</b> — deposits the player's current in-level progress
 *       into the syringe, leaving them at exactly the start of that level. If the
 *       player is at an exact level boundary, deposits the full previous level's XP.</li>
 * </ul>
 *
 * <p>Capacity is {@value #CAPACITY} XP points (≈ 32 buckets of XP Juice at the
 * {@value #XP_PER_BUCKET} XP-per-bucket conversion rate). A durability bar shows
 * the fill level in the inventory.
 */
public class ExperienceSyringeItem extends Item {

    /** XP points stored per bucket of XP fluid (1 bucket = 1 000 mB = 50 XP points). */
    public static final int XP_PER_BUCKET = 50;
    /** Total capacity in XP points (32 buckets). */
    public static final int CAPACITY = 32 * XP_PER_BUCKET; // 1 600

    /** Converts XP points to mB for fluid system display (1 XP = 20 mB). */
    public static int xpToMb(int xp) {
        return xp * 1000 / XP_PER_BUCKET;
    }

    /** Converts mB to XP points, clamped to {@link #CAPACITY} to avoid overflow. */
    public static int mbToXp(int mb) {
        return (int) Math.min((long) mb * XP_PER_BUCKET / 1000, CAPACITY);
    }

    public ExperienceSyringeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int stored = stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);

        if (player.isShiftKeyDown()) {
            // Deposit: extract current-level progress into the syringe.
            int progressXp = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
            if (progressXp == 0 && player.experienceLevel > 0) {
                // At an exact level boundary — deposit the full previous level's worth.
                progressXp = xpNeededForLevel(player.experienceLevel - 1);
            }
            int toStore = Math.min(progressXp, CAPACITY - stored);
            if (toStore > 0) {
                player.giveExperiencePoints(-toStore);
                stack.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), stored + toStore);
                playXpSound(level, player);
            }
        } else {
            // Withdraw: give the player exactly enough XP to reach their next whole level.
            int progressXp = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
            int neededForNext = player.getXpNeededForNextLevel() - progressXp;
            int toGive = Math.min(neededForNext, stored);
            if (toGive > 0) {
                player.giveExperiencePoints(toGive);
                stack.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), stored - toGive);
                playXpSound(level, player);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void playXpSound(Level level, Player player) {
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.1f,
                0.5f + level.getRandom().nextFloat() * 0.1f);
    }

    /** XP required to advance from {@code level} to {@code level + 1}, matching vanilla formula. */
    private static int xpNeededForLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 16) return 37 + (level - 16) * 5;
        return 7 + level * 2;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay tooltip,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag) {
        int stored = stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
        int storedMb   = xpToMb(stored);
        int capacityMb = xpToMb(CAPACITY);
        tooltipAdder.accept(
                Component.translatable(
                        "item.mobfarmingsupplies.experience_syringe.stored",
                        storedMb, capacityMb, stored)
                        .withStyle(stored == 0 ? ChatFormatting.DARK_GRAY : ChatFormatting.GREEN));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int stored = stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
        return Math.round(13f * stored / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Neon green, matching the XP Juice fluid tint.
        return 0x39FF14;
    }
}
