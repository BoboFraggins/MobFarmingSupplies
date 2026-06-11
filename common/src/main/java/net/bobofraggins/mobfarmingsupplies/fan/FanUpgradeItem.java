package net.bobofraggins.mobfarmingsupplies.fan;

import net.minecraft.world.item.Item;

/**
 * One of the three Fan upgrade items: Width, Height, or Distance.
 *
 * <p>Each item type is placed in a dedicated upgrade slot in the {@link FanMenu}.
 * The slot only accepts the specific item registered for that slot, so Width items
 * cannot be put in the Height slot, etc.
 *
 * <ul>
 *   <li>{@link UpgradeType#WIDTH}    — each item in slot 0 expands the push AABB ±1 block
 *       on the axis perpendicular to FACING (within the horizontal plane).</li>
 *   <li>{@link UpgradeType#HEIGHT}   — each item in slot 1 expands the push AABB ±1 block
 *       vertically (Y axis).</li>
 *   <li>{@link UpgradeType#DISTANCE} — each item in slot 2 adds 1 block of push depth
 *       in the FACING direction.</li>
 * </ul>
 */
public class FanUpgradeItem extends Item {

    public enum UpgradeType { WIDTH, HEIGHT, DISTANCE }

    private final UpgradeType upgradeType;

    public FanUpgradeItem(UpgradeType type, Properties props) {
        super(props);
        this.upgradeType = type;
    }

    public UpgradeType getUpgradeType() {
        return upgradeType;
    }
}
