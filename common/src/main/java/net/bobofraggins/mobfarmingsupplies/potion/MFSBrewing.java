package net.bobofraggins.mobfarmingsupplies.potion;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.oresight.OresightTargets;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Our brewing stand recipes, for both loaders: NeoForge registers them as a brewing recipe, and
 * on Fabric mixins add them to vanilla's brewing.
 *
 * <ul>
 *   <li>Essence of Air bottle + Elytra → Lesser Potion of Flight
 *   <li>Essence of Sight bottle + an ore (or its raw ore, gem or dust) → Lesser Potion of Oresight for that ore
 *   <li>Lesser potion + Redstone → Greater; Greater potion + Block of Redstone → Superior
 * </ul>
 *
 * Which recipe applies depends on what's in the bottle slot too, so Redstone makes Oresight for
 * Redstone Ore from an Essence of Sight bottle and improves a Lesser potion.
 */
public final class MFSBrewing {

    /** What an Essence of Sight bottle takes to make Oresight: ores, raw ores, gems and dusts. */
    public static final TagKey<Item> ORESIGHT_INPUTS =
            TagKey.create(Registries.ITEM, new ResourceLocation("mobfarmingsupplies", "oresight_inputs"));

    private MFSBrewing() {}

    /** Whether {@code stack} goes in a bottle slot for one of our recipes. */
    public static boolean isInput(ItemStack stack) {
        return isEssence(stack, Essence.AIR) || isEssence(stack, Essence.SIGHT)
                || stack.getItem() instanceof TieredPotionItem;
    }

    /** Whether {@code stack} goes in the ingredient slot for one of our recipes. */
    public static boolean isIngredient(ItemStack stack) {
        return stack.is(Items.ELYTRA) || stack.is(Items.REDSTONE) || stack.is(Items.REDSTONE_BLOCK)
                || ore(stack) != null;
    }

    /** What brewing {@code ingredient} into {@code input} makes, or empty if nothing. */
    public static ItemStack mix(ItemStack input, ItemStack ingredient) {
        if (input.isEmpty() || ingredient.isEmpty()) return ItemStack.EMPTY;
        if (isEssence(input, Essence.AIR)) {
            return ingredient.is(Items.ELYTRA) ? TieredPotionItem.flight(PotionTier.LESSER) : ItemStack.EMPTY;
        }
        if (isEssence(input, Essence.SIGHT)) {
            Block ore = ore(ingredient);
            return ore == null ? ItemStack.EMPTY : TieredPotionItem.oresight(PotionTier.LESSER, ore);
        }
        if (input.getItem() instanceof TieredPotionItem potion) {
            PotionTier next = potion.tier().upgradedBy(ingredient);
            return next == null ? ItemStack.EMPTY : potion.withTier(input, next);
        }
        return ItemStack.EMPTY;
    }

    private static boolean isEssence(ItemStack stack, Essence essence) {
        return stack.is(Registration.ESSENCE_BOTTLES.get(essence).get());
    }

    /** The ore {@code stack} finds with Oresight, or null. */
    private static Block ore(ItemStack stack) {
        return stack.is(ORESIGHT_INPUTS) ? OresightTargets.oreFor(stack) : null;
    }
}
