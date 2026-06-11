package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Upgrade item for the Mob Harvester.
 *
 * <p>Three typed variants correspond to the three upgrade slots:
 * <ul>
 *   <li>{@link UpgradeType#SHARPNESS} (slot 0) — increases attack damage
 *       via Sharpness × (count × 10)</li>
 *   <li>{@link UpgradeType#LOOTING}   (slot 1) — improves drop rates
 *       via Looting × count</li>
 *   <li>{@link UpgradeType#BEHEADING} (slot 2) — adds a chance to drop mob heads</li>
 * </ul>
 *
 * <p>Each slot accepts up to {@link net.bobofraggins.mobfarmingsupplies.MGRConfig#getHarvesterMaxUpgrade()} items of
 * its own type.  The slot enforces this via {@link MobHarvesterMenu}'s typed slot.
 */
public class HarvesterUpgradeItem extends Item {

    public enum UpgradeType {
        SHARPNESS(10),
        LOOTING(10),
        BEHEADING(5);

        /** Maximum items accepted by this upgrade slot. */
        public final int maxStack;

        UpgradeType(int maxStack) { this.maxStack = maxStack; }
    }

    public final UpgradeType upgradeType;

    public HarvesterUpgradeItem(Properties props, UpgradeType type) {
        super(props);
        this.upgradeType = type;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable(
                "item.mobfarmingsupplies.harvester_upgrade_"
                        + upgradeType.name().toLowerCase() + ".tooltip",
                upgradeType.maxStack).withStyle(ChatFormatting.GRAY));
    }
}
