package net.bobofraggins.mobfarmingsupplies.itemfilter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Builds the list of {@link ItemMatcher}s offered by the Filter Scribing Terminal for a given
 * item. The list is deterministic — the client builds it for display and the server rebuilds it
 * to resolve the selected row index — so order never depends on translated text.
 *
 * <p>Order: exactly this item, a similar item, vanilla / from mod, then every
 * {@link #CURATED_TAGS curated tag} the item is in (most specific first), then the
 * {@link ItemMatcher.Property properties} it has.
 */
public final class ItemMatchers {

    private ItemMatchers() {}

    /**
     * Tags offered as matchers, most specific first. Only tags defined by both NeoForge and
     * Fabric (the shared {@code c:} set), plus vanilla tags where {@code c:} has no equivalent
     * (e.g. swords, armor slots). Each needs a {@code filter.mobfarmingsupplies.tag.*} lang entry.
     */
    static final List<Identifier> CURATED_TAGS = Stream.of(
            // Tool and weapon types
            "minecraft:swords", "minecraft:axes", "minecraft:pickaxes", "minecraft:shovels", "minecraft:hoes",
            "minecraft:spears", "c:tools/bow", "c:tools/crossbow", "c:tools/trident", "c:tools/mace",
            "c:tools/shield", "c:tools/fishing_rod", "c:tools/shear", "c:tools/brush", "c:tools/igniter",
            "c:tools/wrench", "c:tools/melee_weapon", "c:tools/ranged_weapon", "c:tools/mining_tool", "c:tools",
            // Armor
            "minecraft:head_armor", "minecraft:chest_armor", "minecraft:leg_armor", "minecraft:foot_armor",
            "c:armors/horse", "c:armors/wolf", "c:armors", "minecraft:arrows",
            // Food and drink
            "c:foods/raw_meat", "c:foods/cooked_meat", "c:foods/raw_fish", "c:foods/cooked_fish",
            "c:foods/fruit", "c:foods/vegetable", "c:foods/berry", "c:foods/bread", "c:foods/cookie",
            "c:foods/candy", "c:foods/pie", "c:foods/soup", "c:foods/golden", "c:foods/food_poisoning",
            "c:foods", "c:animal_foods",
            "c:drinks/milk", "c:drinks/honey", "c:drinks/juice", "c:drinks/magic", "c:drinks/water",
            "c:drinks", "c:potions",
            // Materials
            "c:ores", "c:raw_materials", "c:ingots", "c:nuggets", "c:gems", "c:dusts", "c:storage_blocks",
            "c:rods", "c:dyes", "c:seeds", "c:crops", "c:flowers", "c:mushrooms", "minecraft:saplings",
            "c:natural_logs", "minecraft:planks", "minecraft:wool", "c:stones", "c:cobblestones", "c:sands",
            "c:gravels", "c:glass_blocks", "c:eggs", "c:feathers", "c:leathers", "c:strings", "c:bones",
            "c:slime_balls", "c:ender_pearls", "c:gunpowders", "c:fertilizers",
            // Miscellaneous
            "c:music_discs", "c:buckets", "c:shulker_boxes", "c:chests", "c:barrels", "c:ropes",
            "minecraft:boats", "minecraft:beds", "minecraft:banners")
            .map(Identifier::parse)
            .toList();

    /** All matchers applicable to {@code stack}, or an empty list if it's empty. */
    public static List<ItemMatcher> forItem(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        if (stack.isEmpty()) return List.of();
        List<ItemMatcher> out = new ArrayList<>();
        out.add(ItemMatcher.exact(stack));
        out.add(ItemMatcher.item(stack.getItem()));
        out.add(ItemMatcher.mod(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()));
        for (Identifier tag : CURATED_TAGS) {
            if (stack.is(TagKey.create(Registries.ITEM, tag))) out.add(ItemMatcher.tag(tag));
        }
        for (ItemMatcher.Property p : ItemMatcher.Property.values()) {
            if (p.test(stack, registries)) out.add(ItemMatcher.property(p));
        }
        return out;
    }
}
