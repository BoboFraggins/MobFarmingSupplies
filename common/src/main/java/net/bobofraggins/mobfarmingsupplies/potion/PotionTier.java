package net.bobofraggins.mobfarmingsupplies.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * How strong one of our brewed potions is, which only changes how long it lasts. A Lesser potion
 * is brewed from an essence bottle; Redstone makes it Greater, and a Block of Redstone makes a
 * Greater one Superior.
 */
public enum PotionTier {
    LESSER("lesser", 3),
    GREATER("greater", 8),
    SUPERIOR("superior", 30);

    private final String id;
    private final int durationTicks;

    PotionTier(String id, int minutes) {
        this.id = id;
        this.durationTicks = minutes * 60 * 20;
    }

    public String id() { return id; }

    public int durationTicks() { return durationTicks; }

    /** The tier brewing {@code ingredient} into a potion of this tier gives, or null if it doesn't improve it. */
    @Nullable
    public PotionTier upgradedBy(ItemStack ingredient) {
        return switch (this) {
            case LESSER -> ingredient.is(Items.REDSTONE) ? GREATER : null;
            case GREATER -> ingredient.is(Items.REDSTONE_BLOCK) ? SUPERIOR : null;
            case SUPERIOR -> null;
        };
    }
}
