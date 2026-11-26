package net.bobofraggins.mobfarmingsupplies.mobhead;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Guillotine: a sword (or axe) enchantment, levels I-V, that makes a melee kill drop the victim's
 * head with a chance of level in 10 (see {@link BeheadingDrops}). 1.20.5+ defines it in data
 * ({@code enchantment/guillotine.json}); 1.20.1 needs the class. Same costs and weight.
 */
public class GuillotineEnchantment extends Enchantment {

    public GuillotineEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return 25 + (level - 1) * 8;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || super.canEnchant(stack);
    }
}
