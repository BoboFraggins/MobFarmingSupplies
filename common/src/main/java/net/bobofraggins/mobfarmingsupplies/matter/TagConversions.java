package net.bobofraggins.mobfarmingsupplies.matter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

/**
 * Treats the items in a material tag as interchangeable, so another mod's iron ingot, say, shares a
 * value with the vanilla one through {@code c:ingots/iron} without any setup. Each member is joined
 * both ways to the tag's first member, which ties the whole tag together. The idea follows ProjectE's
 * handling of tags (see {@link MatterGraphSolver}).
 *
 * <p>Only common ({@code c:}) tags for one material in one form count, such as {@code c:ingots/iron}
 * or {@code c:storage_blocks/iron}. Broader tags ({@code c:ingots}, or vanilla's tags of tools that
 * take the same enchantments) group items of very different worth, and joining those would give
 * everything in them the value of the cheapest.
 */
public final class TagConversions {

    private TagConversions() {}

    public static List<MatterConversion> collect(ServerLevel level) {
        List<MatterConversion> conversions = new ArrayList<>();
        level.registryAccess().lookupOrThrow(Registries.ITEM).listTags()
                .forEach(tag -> join(conversions, tag));
        return conversions;
    }

    /** The forms a material tag can be for, as the first part of a {@code c:} tag's path. */
    private static final Set<String> MATERIAL_FORMS = Set.of("ingots", "nuggets", "gems", "dusts",
            "storage_blocks", "raw_materials", "plates", "gears", "rods", "wires", "ores");

    private static boolean isMaterialTag(Identifier id) {
        if (!id.getNamespace().equals("c")) return false;
        String[] parts = id.getPath().split("/");
        return parts.length == 2 && MATERIAL_FORMS.contains(parts[0]);
    }

    private static void join(List<MatterConversion> conversions, HolderSet.Named<Item> tag) {
        if (tag.size() < 2 || !isMaterialTag(tag.key().location())) return;
        Item first = tag.get(0).value();
        for (Holder<Item> member : tag) {
            Item item = member.value();
            if (item == first) continue;
            conversions.add(MatterConversion.oneForOne(first, item));
            conversions.add(MatterConversion.oneForOne(item, first));
        }
    }
}
