package net.bobofraggins.mobfarmingsupplies.matter;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

/**
 * How much matter an item is worth: a measure of what it took to make, for things that consume or
 * produce items by their substance. A few basic materials have set values here (and any the config
 * sets); every other item's value is worked out by {@link MatterGraphSolver} from the recipes that
 * make it, the tags it shares, and the mobs that drop it ({@link DropConversions}). Items with no
 * route back to any of those are worth nothing.
 *
 * <p>The worked-out values are rebuilt when a server starts and whenever datapacks reload, since
 * recipes and tags can change. That only happens on the server, which is the only side that has
 * every recipe; on a client only the set values are known.
 */
public final class MatterValues {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<Item, Integer> SET = new HashMap<>();
    private static volatile Map<Item, Integer> worked = Map.of();

    private MatterValues() {}

    /**
     * Works out values when a server starts. Each loader also calls {@link #rebuild} after a datapack
     * reload, which Architectury has no event for.
     */
    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(server -> rebuild(server.overworld()));
    }

    /** 0 if the item is worth nothing. */
    public static int get(ItemStack stack) {
        return stack.isEmpty() ? 0 : get(stack.getItem());
    }

    public static int get(Item item) {
        Integer value = setValues().get(item);
        return value != null ? value : worked.getOrDefault(item, 0);
    }

    public static boolean has(ItemStack stack) {
        return get(stack) > 0;
    }

    /**
     * The set values. Filled in on first use, not at startup: on NeoForge this mod's items aren't
     * registered yet when it initialises.
     */
    private static synchronized Map<Item, Integer> setValues() {
        if (!SET.isEmpty()) return SET;

        // Common materials
        set(Items.DIRT, 1);
        set(Items.COBBLESTONE, 1);
        set(Items.STONE, 1);
        set(Items.SAND, 1);
        set(Items.GRAVEL, 2);
        set(Items.CLAY_BALL, 1);
        set(Items.GLASS, 2);
        set(Items.STRING, 1);
        set(Items.LEATHER, 3);
        set(Items.OBSIDIAN, 16);
        set(Items.ENDER_PEARL, 8);
        set(Items.BLAZE_ROD, 4);
        set(Items.COAL, 8);
        set(Items.CHARCOAL, 8);

        // Metals and gems
        set(Items.COPPER_INGOT, 12);
        set(Items.IRON_INGOT, 16);
        set(Items.GOLD_INGOT, 24);
        set(Items.REDSTONE, 4);
        set(Items.LAPIS_LAZULI, 4);
        set(Items.QUARTZ, 8);
        set(Items.DIAMOND, 64);
        set(Items.EMERALD, 48);
        set(Items.NETHERITE_SCRAP, 256);
        set(Items.NETHERITE_INGOT, 512);
        set(Items.NETHER_STAR, 1024);

        // This mod's materials that no recipe makes
        set(Registration.RAW_TRITANIUM.get(), 32);
        set(Registration.DILITHIUM_CRYSTAL.get(), 64);
        set(Registration.ZOMBIE_BRAIN.get(), 4);
        set(Registration.BRAIN.get(), 6);

        // The config's own values, last so they win
        for (String entry : MFSConfig.getMatterValues()) {
            int equals = entry.lastIndexOf('=');
            Identifier id = equals > 0 ? Identifier.tryParse(entry.substring(0, equals).strip()) : null;
            Integer value = equals > 0 ? parse(entry.substring(equals + 1).strip()) : null;
            if (id == null || value == null) {
                LOGGER.warn("Ignoring matter value \"{}\": expected item_id=value", entry);
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(id).ifPresentOrElse(item -> set(item, value),
                    () -> LOGGER.warn("Ignoring matter value for unknown item {}", id));
        }
        return SET;
    }

    private static void set(Item item, int value) {
        SET.put(item, value);
    }

    private static Integer parse(String text) {
        try {
            int value = Integer.parseInt(text);
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Works out every other item's value from the current recipes and tags. */
    public static void rebuild(ServerLevel level) {
        Map<Item, Long> known = new HashMap<>();
        setValues().forEach((item, value) -> known.put(item, (long) value));

        List<MatterConversion> conversions = new ArrayList<>(RecipeConversions.collect(level));
        conversions.addAll(TagConversions.collect(level));
        conversions.addAll(DropConversions.collect(level));
        List<String> ignoredItems = MFSConfig.getMatterIgnoredItems();
        List<String> ignoredMods = MFSConfig.getMatterIgnoredMods();
        if (!ignoredItems.isEmpty() || !ignoredMods.isEmpty()) {
            conversions.removeIf(conversion -> involves(conversion, ignoredItems, ignoredMods));
        }

        Map<Item, Integer> values = new HashMap<>();
        new MatterGraphSolver(known).solve(conversions)
                .forEach((item, value) -> values.put(item, (int) Math.min(value, Integer.MAX_VALUE)));
        worked = Map.copyOf(values);
        LOGGER.info("Worked out matter values for {} items from {} recipes and tags ({} set values)",
                worked.size(), conversions.size(), SET.size());
    }

    /** Whether a conversion makes or uses an ignored item, or one from an ignored mod. */
    private static boolean involves(MatterConversion conversion, List<String> items, List<String> mods) {
        if (ignored(conversion.output(), items, mods)) return true;
        for (MatterConversion.Input input : conversion.inputs()) {
            for (Item item : input.alternatives()) {
                if (ignored(item, items, mods)) return true;
            }
        }
        return false;
    }

    private static boolean ignored(Item item, List<String> items, List<String> mods) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return items.contains(id.toString()) || mods.contains(id.getNamespace());
    }
}
