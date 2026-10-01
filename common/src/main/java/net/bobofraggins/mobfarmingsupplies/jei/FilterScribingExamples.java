package net.bobofraggins.mobfarmingsupplies.jei;

import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemFilterData;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatcher;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatchers;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;

/**
 * The examples cycled through by the Filter Scribing JEI recipe: an item placed in the matcher
 * slot, the Is / Is Not setting, which row of the matcher list is selected, and the resulting
 * Item Filter. Built from the real {@link ItemMatchers} list so the drawn rows match the terminal.
 */
public final class FilterScribingExamples {

    /** One example. {@code rows} are the list labels shown for {@code matcherItem}. */
    public record Example(ItemStack matcherItem, List<Component> rows, int selectedRow, boolean negate, ItemStack filter) {}

    /** The full set of examples, cycled through together as one JEI recipe. */
    public record Recipe(List<Example> examples) {}

    private record Spec(ItemStack item, boolean negate, Predicate<ItemMatcher> pick) {}

    private FilterScribingExamples() {}

    public static Recipe create() {
        ItemStack excalibur = new ItemStack(Items.DIAMOND_SWORD);
        excalibur.set(DataComponents.CUSTOM_NAME, Component.literal("Excalibur"));

        // Is / Is Not is varied across the list so both toggle states are shown.
        List<Spec> specs = List.of(
                new Spec(excalibur, false, kind(ItemMatcher.Kind.EXACT)),
                new Spec(new ItemStack(Items.IRON_PICKAXE), true, kind(ItemMatcher.Kind.ITEM)),
                new Spec(new ItemStack(Items.DIAMOND_SWORD), false, tag("minecraft:swords")),
                new Spec(new ItemStack(Items.COBBLESTONE), true, kind(ItemMatcher.Kind.MOD)),
                new Spec(new ItemStack(Items.IRON_INGOT), false, tag("c:ingots")),
                new Spec(new ItemStack(Items.CARROT), true, tag("c:foods/vegetable")),
                new Spec(new ItemStack(Items.BOW), false, property(ItemMatcher.Property.WEAPON)),
                new Spec(new ItemStack(Registration.SILICON.get()), false, kind(ItemMatcher.Kind.MOD)),
                new Spec(new ItemStack(Items.OAK_LOG), true, tag("c:natural_logs")),
                new Spec(new ItemStack(Items.GOLDEN_HELMET), false, property(ItemMatcher.Property.ENCHANTABLE)),
                new Spec(new ItemStack(Items.DIAMOND_ORE), false, tag("c:ores")),
                new Spec(new ItemStack(Items.SHEARS), true, property(ItemMatcher.Property.DAMAGEABLE)));

        var level = Minecraft.getInstance().level;
        HolderLookup.Provider registries = level != null ? level.registryAccess() : null;
        List<Example> examples = new ArrayList<>();
        for (Spec spec : specs) {
            List<ItemMatcher> matchers = ItemMatchers.forItem(spec.item(), registries);
            int row = -1;
            for (int i = 0; i < matchers.size(); i++) {
                if (spec.pick().test(matchers.get(i))) {
                    row = i;
                    break;
                }
            }
            if (row < 0) continue; // e.g. a tag another mod removed — just skip this example
            ItemStack filter = new ItemStack(Registration.ITEM_FILTER.get());
            filter.set(Registration.ITEM_FILTER_DATA.get(), new ItemFilterData(matchers.get(row), spec.negate()));
            examples.add(new Example(spec.item(), matchers.stream().map(ItemMatcher::listLabel).toList(),
                    row, spec.negate(), filter));
        }
        return new Recipe(examples);
    }

    private static Predicate<ItemMatcher> kind(ItemMatcher.Kind kind) {
        return m -> m.kind() == kind;
    }

    private static Predicate<ItemMatcher> tag(String tag) {
        return m -> m.kind() == ItemMatcher.Kind.TAG && m.key().equals(tag);
    }

    private static Predicate<ItemMatcher> property(ItemMatcher.Property property) {
        return m -> m.kind() == ItemMatcher.Kind.PROPERTY && m.key().equals(property.id());
    }
}
