package net.bobofraggins.mobfarmingsupplies.matter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Prices what mobs drop by how hard the mob is to come by. Each mob gets a {@link Tier}, worth a set
 * amount for each item it drops; an item's price from that mob is the tier's worth divided by how
 * many of it one kill gives on average. An item dropped by several mobs, or also craftable, takes
 * its cheapest source.
 *
 * <p>A mob's tier comes from, in order: the {@code c:bosses} tag; the DNA Sample Pack lists in the
 * config (Common, Aquatic, Rare and Nether); then where it spawns naturally. A mob that spawns in the
 * Overworld is common, aquatic if it's a water mob. One that spawns only in other dimensions (the
 * Nether, the End, or another mod's) is worth the same as a Nether mob. One that never spawns
 * naturally is rare. Things that aren't really mobs, like armour stands, are left out.
 *
 * <p>The average is read from each mob's loot table. Chances and counts are taken without Looting.
 * A drop that depends on the mob itself, such as a slime's size, counts half the time; one that
 * depends on circumstances (a creeper killed by a skeleton, a zombie riding a horse) is left out.
 * Where a table picks one of several variants (a sheep's colour), they share the drop.
 */
public final class DropConversions {

    /** How hard a mob is to come by, and what each item it drops is worth. */
    public enum Tier {
        COMMON_PASSIVE(2), COMMON_HOSTILE(4), AQUATIC(4), RARE_PASSIVE(8), RARE_HOSTILE(16), OTHER_DIMENSION(16),
        BOSS(256);

        final int worth;

        Tier(int worth) {
            this.worth = worth;
        }
    }

    private static final TagKey<EntityType<?>> BOSSES =
            TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "bosses"));
    private static final Set<MobCategory> WATER = Set.of(MobCategory.WATER_CREATURE, MobCategory.WATER_AMBIENT,
            MobCategory.UNDERGROUND_WATER_CREATURE, MobCategory.AXOLOTLS);
    /** Below this many per kill, a drop is too rare to price. */
    private static final double RAREST = 0.0001;

    private final ServerLevel level;
    private final Map<EntityType<?>, Boolean> spawnsInOverworld = new HashMap<>();

    private DropConversions(ServerLevel level) {
        this.level = level;
    }

    public static List<MatterConversion> collect(ServerLevel level) {
        return new DropConversions(level).collect();
    }

    private List<MatterConversion> collect() {
        findNaturalSpawns();
        Map<EntityType<?>, Tier> packs = packTiers();
        List<MatterConversion> conversions = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Tier tier = tier(type, packs);
            if (tier == null) continue;
            type.getDefaultLootTable().ifPresent(key -> {
                Map<Item, Double> perKill = new HashMap<>();
                table(key, 1, perKill, 0);
                perKill.forEach((item, amount) -> {
                    if (amount >= RAREST) {
                        conversions.add(MatterConversion.fixed(item, (long) Math.ceil(tier.worth / amount)));
                    }
                });
            });
        }
        return conversions;
    }

    // ── Tiers ─────────────────────────────────────────────────────────────────────

    private Tier tier(EntityType<?> type, Map<EntityType<?>, Tier> packs) {
        if (type.builtInRegistryHolder().is(BOSSES)) return Tier.BOSS;
        Tier fromPack = packs.get(type);
        if (fromPack != null) return fromPack;
        MobCategory category = type.getCategory();
        if (category == MobCategory.MISC) return null;
        Boolean overworld = spawnsInOverworld.get(type);
        boolean hostile = category == MobCategory.MONSTER;
        if (overworld == null) return hostile ? Tier.RARE_HOSTILE : Tier.RARE_PASSIVE;
        if (!overworld) return Tier.OTHER_DIMENSION;
        if (WATER.contains(category)) return Tier.AQUATIC;
        return hostile ? Tier.COMMON_HOSTILE : Tier.COMMON_PASSIVE;
    }

    /** The DNA Sample Pack lists; a mob in more than one takes the most valuable. */
    private static Map<EntityType<?>, Tier> packTiers() {
        Map<Tier, List<String>> lists = new EnumMap<>(Tier.class);
        lists.put(Tier.COMMON_PASSIVE, MFSConfig.getCommonPassivePackMobs());
        lists.put(Tier.COMMON_HOSTILE, MFSConfig.getCommonHostilePackMobs());
        lists.put(Tier.AQUATIC, MFSConfig.getAquaticPackMobs());
        lists.put(Tier.RARE_PASSIVE, MFSConfig.getRarePassivePackMobs());
        lists.put(Tier.RARE_HOSTILE, MFSConfig.getRareHostilePackMobs());
        lists.put(Tier.OTHER_DIMENSION, MFSConfig.getNetherPackMobs());
        Map<EntityType<?>, Tier> tiers = new HashMap<>();
        lists.forEach((tier, ids) -> {
            for (String id : ids) {
                Identifier key = Identifier.tryParse(id);
                if (key == null) continue;
                BuiltInRegistries.ENTITY_TYPE.getOptional(key).ifPresent(type -> tiers.merge(type, tier,
                        (a, b) -> a.worth >= b.worth ? a : b));
            }
        });
        return tiers;
    }

    /** Which mobs spawn naturally, and whether any of them in the Overworld. */
    private void findNaturalSpawns() {
        level.registryAccess().lookupOrThrow(Registries.BIOME).listElements().forEach(biome -> {
            boolean overworld = biome.is(BiomeTags.IS_OVERWORLD);
            MobSpawnSettings spawns = biome.value().getMobSettings();
            for (MobCategory category : MobCategory.values()) {
                for (Weighted<MobSpawnSettings.SpawnerData> spawn : spawns.getMobs(category).unwrap()) {
                    spawnsInOverworld.merge(spawn.value().type(), overworld, Boolean::logicalOr);
                }
            }
        });
    }

    // ── Loot tables ───────────────────────────────────────────────────────────────

    /** Adds what {@code key}'s table gives, {@code times} over, to {@code perKill}. */
    private void table(ResourceKey<LootTable> key, double times, Map<Item, Double> perKill, int depth) {
        if (depth > 8) return;
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootTable.DIRECT_CODEC.encodeStart(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), table)
                .ifSuccess(json -> table(json, times, perKill, depth));
    }

    private void table(JsonElement json, double times, Map<Item, Double> perKill, int depth) {
        if (!json.isJsonObject() || !json.getAsJsonObject().has("pools")) return;
        for (JsonElement pool : json.getAsJsonObject().getAsJsonArray("pools")) {
            JsonObject object = pool.getAsJsonObject();
            double chance = chance(object);
            if (chance <= 0) continue;
            JsonArray entries = object.getAsJsonArray("entries");
            if (entries == null) continue;
            double totalWeight = 0;
            for (JsonElement entry : entries) totalWeight += weight(entry.getAsJsonObject());
            if (totalWeight <= 0) continue;
            double rolls = average(object.get("rolls"), 1);
            for (JsonElement entry : entries) {
                JsonObject e = entry.getAsJsonObject();
                entry(e, times * chance * rolls * weight(e) / totalWeight, perKill, depth, false);
            }
        }
    }

    /** Adds one entry's items; {@code variant} when it's one of several a table picks between. */
    private void entry(JsonObject entry, double times, Map<Item, Double> perKill, int depth, boolean variant) {
        double chance = variant ? passableChance(entry) : chance(entry);
        if (chance <= 0) return;
        double amount = times * chance;
        String type = string(entry, "type");
        switch (type) {
            case "minecraft:item" -> {
                Identifier id = Identifier.tryParse(string(entry, "name"));
                if (id == null) return;
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(item ->
                        perKill.merge(item, amount * count(entry), Double::sum));
            }
            case "minecraft:tag" -> {
                Identifier id = Identifier.tryParse(string(entry, "name"));
                if (id == null) return;
                List<Holder<Item>> items = new ArrayList<>();
                BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, id)).forEach(items::add);
                if (items.isEmpty()) return;
                boolean oneOf = entry.has("expand") && entry.get("expand").getAsBoolean();
                double each = amount * count(entry) / (oneOf ? items.size() : 1);
                for (Holder<Item> item : items) perKill.merge(item.value(), each, Double::sum);
            }
            case "minecraft:loot_table" -> {
                JsonElement value = entry.get("value");
                if (value == null) return;
                if (value.isJsonPrimitive()) {
                    Identifier id = Identifier.tryParse(value.getAsString());
                    if (id != null) table(ResourceKey.create(Registries.LOOT_TABLE, id), amount, perKill, depth + 1);
                } else {
                    table(value, amount, perKill, depth + 1);
                }
            }
            case "minecraft:alternatives", "minecraft:group", "minecraft:sequence" -> {
                JsonArray children = entry.getAsJsonArray("children");
                if (children == null || children.isEmpty()) return;
                // One of several (usually exclusive) variants: they share the drop between them.
                double share = type.equals("minecraft:group") ? 1 : 1.0 / children.size();
                for (JsonElement child : children) {
                    entry(child.getAsJsonObject(), amount * share, perKill, depth, !type.equals("minecraft:group"));
                }
            }
            default -> { }
        }
    }

    private static double weight(JsonObject entry) {
        return entry.has("weight") ? entry.get("weight").getAsDouble() : 1;
    }

    /** The chance an entry or pool's conditions pass; 0 if it depends on circumstances. */
    private static double chance(JsonObject object) {
        return conditions(object, false);
    }

    /** As {@link #chance}, but circumstances only pick between variants, so they count as passing. */
    private static double passableChance(JsonObject object) {
        return conditions(object, true);
    }

    private static double conditions(JsonObject object, boolean variant) {
        if (!object.has("conditions")) return 1;
        double chance = 1;
        for (JsonElement condition : object.getAsJsonArray("conditions")) {
            chance *= condition(condition.getAsJsonObject(), variant);
        }
        return chance;
    }

    private static double condition(JsonObject condition, boolean variant) {
        return switch (string(condition, "condition")) {
            case "minecraft:random_chance" -> average(condition.get("chance"), 1);
            case "minecraft:random_chance_with_enchanted_bonus" -> average(condition.get("unenchanted_chance"), 1);
            case "minecraft:damage_source_properties" -> variant ? 1 : 0;
            case "minecraft:entity_properties" -> variant ? 1 : mobProperty(condition);
            // Negated circumstances (not a baby, say) usually hold.
            case "minecraft:inverted" -> 1;
            default -> 1;
        };
    }

    /**
     * A condition on the mob: a property of the mob itself (its size, say) is a variant that holds
     * about half the time; what it's riding or carrying is a rare circumstance, left out.
     */
    private static double mobProperty(JsonObject condition) {
        JsonElement predicate = condition.get("predicate");
        if (predicate == null || !predicate.isJsonObject()) return 0;
        for (String situational : List.of("vehicle", "passenger", "targeted_entity", "stepping_on")) {
            if (predicate.getAsJsonObject().has(situational)) return 0;
        }
        return 0.5;
    }

    /** How many items an entry gives, from its {@code set_count}, never below 0. */
    private static double count(JsonObject entry) {
        if (!entry.has("functions")) return 1;
        double count = 1;
        for (JsonElement function : entry.getAsJsonArray("functions")) {
            JsonObject f = function.getAsJsonObject();
            if (string(f, "function").equals("minecraft:set_count") && !f.has("conditions")) {
                boolean add = f.has("add") && f.get("add").getAsBoolean();
                double value = atLeastZero(f.get("count"));
                count = add ? count + value : value;
            }
        }
        return count;
    }

    /** The average of a number provider. */
    private static double average(JsonElement number, double fallback) {
        if (number == null) return fallback;
        if (number.isJsonPrimitive()) return number.getAsDouble();
        if (!number.isJsonObject()) return fallback;
        JsonObject o = number.getAsJsonObject();
        return switch (string(o, "type")) {
            case "minecraft:constant" -> average(o.get("value"), fallback);
            case "minecraft:uniform" -> (average(o.get("min"), 0) + average(o.get("max"), 0)) / 2;
            case "minecraft:binomial" -> average(o.get("n"), 0) * average(o.get("p"), 0);
            default -> fallback;
        };
    }

    /** The average of a number provider whose negative results count as 0, as an item count's do. */
    private static double atLeastZero(JsonElement number) {
        if (number != null && number.isJsonObject() && string(number.getAsJsonObject(), "type").equals("minecraft:uniform")) {
            double min = average(number.getAsJsonObject().get("min"), 0);
            double max = average(number.getAsJsonObject().get("max"), 0);
            if (max <= 0) return 0;
            if (min < 0) return max * max / (2 * (max - min));
            return (min + max) / 2;
        }
        return Math.max(0, average(number, 1));
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }
}
