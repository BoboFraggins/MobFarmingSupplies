package net.bobofraggins.mobfarmingsupplies.tritanium;

import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.glamping.NaturalSpawnRolls;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Gives mobs that spawn holding a vanilla sword, axe or shovel a chance to hold a Tritanium Alloy
 * one instead (10%), or a Tritanium Alloy Paxel (10%). Rolled once a mob's spawn setup has handed
 * out its weapon (see {@code MobFinalizeSpawnMixin}); NeoForge's spawn event fires before that, so
 * it can't be used. Commands, spawn eggs, buckets, dispensers and the Clone-O-Matic never roll.
 */
public final class TritaniumMobEquipment {

    private static final float PAXEL_CHANCE = 0.10f;
    private static final float TOOL_CHANCE = 0.10f;
    /** Vanilla's chance for a mob's hand item to drop. */
    private static final float DROP_CHANCE = 0.085f;

    private static final Set<Item> SWORDS = Set.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.COPPER_SWORD,
            Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
    private static final Set<Item> AXES = Set.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.COPPER_AXE,
            Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
    private static final Set<Item> SHOVELS = Set.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.COPPER_SHOVEL,
            Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);

    /**
     * How many spawn setups are running on this thread. A mob's setup can run its parent class's
     * (a Wither Skeleton's runs the skeleton one), and only the outermost may roll.
     */
    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private TritaniumMobEquipment() {}

    /** Called as a mob's spawn setup starts. */
    public static void enterSpawn() {
        DEPTH.get()[0]++;
    }

    /** Called as a mob's spawn setup ends; rolls for a Tritanium Alloy tool if it was the outermost. */
    public static void exitSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty,
                                 EntitySpawnReason reason) {
        if (--DEPTH.get()[0] == 0) afterSpawn(mob, level, difficulty, reason);
    }

    private static void afterSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty,
                                   EntitySpawnReason reason) {
        if (reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.SPAWN_ITEM_USE
                || reason == EntitySpawnReason.BUCKET || reason == EntitySpawnReason.DISPENSER) return;
        if (NaturalSpawnRolls.suppressed() || !MFSConfig.getTritaniumMobsCarryTools()) return;

        Item held = mob.getMainHandItem().getItem();
        if (!SWORDS.contains(held) && !AXES.contains(held) && !SHOVELS.contains(held)) return;
        RandomSource random = mob.getRandom();
        float roll = random.nextFloat();
        Item replacement;
        if (roll < PAXEL_CHANCE) {
            replacement = Registration.TRITANIUM_ALLOY_PAXEL.get();
        } else if (roll < PAXEL_CHANCE + TOOL_CHANCE) {
            replacement = SWORDS.contains(held) ? Registration.TRITANIUM_ALLOY_SWORD.get()
                    : AXES.contains(held) ? Registration.TRITANIUM_ALLOY_AXE.get()
                    : Registration.TRITANIUM_ALLOY_SHOVEL.get();
        } else {
            return;
        }

        // Enchanted as vanilla enchants a mob's gear: 25% x the regional difficulty multiplier.
        ItemStack stack = new ItemStack(replacement);
        float special = difficulty.getSpecialMultiplier();
        if (difficulty.getDifficulty() != Difficulty.PEACEFUL && random.nextFloat() < 0.25f * special) {
            stack = EnchantmentHelper.enchantItem(random, stack, (int) (5 + special * random.nextInt(18)),
                    level.registryAccess(), level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .get(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT));
        }
        mob.setItemInHand(InteractionHand.MAIN_HAND, stack);
        mob.setDropChance(EquipmentSlot.MAINHAND, DROP_CHANCE);
    }
}
