package net.bobofraggins.mobfarmingsupplies;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Server-side configuration for MobFarmingSupplies.
 *
 * <p>Registered from {@link MobFarmingSupplies} via
 * {@code modContainer.registerConfig(ModConfig.Type.SERVER, MGRServerConfig.SPEC)}.
 * Values are available after the server starts and the config file is loaded.
 */
public final class MGRServerConfig {

    public static final ModConfigSpec SPEC;

    // ── Fan ─────────────────────────────────────────────────────────────────────

    /**
     * When {@code false} (default), the fan's push range is blocked by any non-air block.
     * When {@code true}, only blocks with a non-empty collision shape stop the fan's range.
     */
    public static final ModConfigSpec.BooleanValue FAN_STRONGER_BLADES;

    // ── Mob Harvester ───────────────────────────────────────────────────────────────

    /** Maximum number of upgrades the Mob Harvester will accept per slot (0–10). */
    public static final ModConfigSpec.IntValue HARVESTER_MAX_UPGRADE;

    // ── Clone-O-Matic ────────────────────────────────────────────────────────────

    /** Ticks between spawn attempts while the Clone-O-Matic is continuously powered (1–200). */
    public static final ModConfigSpec.IntValue CLONE_O_MATIC_SPAWN_INTERVAL;

    // ── DNA Sample Packs ─────────────────────────────────────────────────────────

    /**
     * Entity type IDs included in the "DNA Collector Pack (Common/Hostile)" sample.
     * Each entry must be a valid entity type resource location, e.g. {@code "minecraft:zombie"}.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> COMMON_HOSTILE_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Collector Pack (Common/Passive)" sample.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> COMMON_PASSIVE_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Collector Pack (Aquatic)" sample.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> AQUATIC_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Collector Pack (Rare/Hostile)" sample.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RARE_HOSTILE_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Collector Pack (Rare/Passive)" sample.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RARE_PASSIVE_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Booster Pack (Nether)" sample.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> NETHER_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Booster Pack (Baby)" sample.
     * Every mob spawned from this pack is forced into its baby form.
     * List is pre-calculated from all mobs confirmed to have baby variants in 26.1.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BABY_PACK_MOBS;

    /**
     * Entity type IDs included in the "DNA Booster Pack (Wrong Mobs Only)" sample.
     * Mobs that have baby variants are forced into their adult form.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WRONG_MOBS_PACK_MOBS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Fan settings").push("fan");
        FAN_STRONGER_BLADES = b
                .comment("If true, the fan's push range is only blocked by solid collision blocks.",
                         "If false (default), any non-air block cuts off the range.")
                .define("strongerBlades", false);
        b.pop();

        b.comment("Mob Harvester settings").push("mobHarvester");
        HARVESTER_MAX_UPGRADE = b
                .comment("Maximum upgrade stack size accepted per slot (0–10).")
                .defineInRange("maxUpgrade", 10, 0, 10);
        b.pop();

        b.comment("Clone-O-Matic settings").push("cloneOMatic");
        CLONE_O_MATIC_SPAWN_INTERVAL = b
                .comment("Ticks between spawn attempts while continuously powered (1–200).")
                .defineInRange("spawnInterval", 5, 1, 200);
        b.pop();

        b.comment("DNA Sample Pack mob lists").push("dnaSamplePacks");

        b.push("commonHostile");
        COMMON_HOSTILE_PACK_MOBS = b
                .comment("Entity types in the 'DNA Collector Pack (Common/Hostile)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:zombie\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:zombie",
                                "minecraft:zombie_villager",
                                "minecraft:skeleton",
                                "minecraft:enderman",
                                "minecraft:spider",
                                "minecraft:slime",
                                "minecraft:creeper",
                                "minecraft:witch"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("commonPassive");
        COMMON_PASSIVE_PACK_MOBS = b
                .comment("Entity types in the 'DNA Collector Pack (Common/Passive)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:cow\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:cow",
                                "minecraft:pig",
                                "minecraft:sheep",
                                "minecraft:chicken",
                                "minecraft:mooshroom",
                                "minecraft:rabbit"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("aquatic");
        AQUATIC_PACK_MOBS = b
                .comment("Entity types in the 'DNA Collector Pack (Aquatic)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:cod\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:drowned",
                                "minecraft:guardian",
                                "minecraft:squid",
                                "minecraft:glow_squid",
                                "minecraft:nautilus",
                                "minecraft:dolphin",
                                "minecraft:cod",
                                "minecraft:salmon",
                                "minecraft:tropical_fish",
                                "minecraft:pufferfish"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("rareHostile");
        RARE_HOSTILE_PACK_MOBS = b
                .comment("Entity types in the 'DNA Collector Pack (Rare/Hostile)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:breeze\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:breeze",
                                "minecraft:evoker",
                                "minecraft:phantom",
                                "minecraft:pillager",
                                "minecraft:vindicator",
                                "minecraft:cave_spider",
                                "minecraft:husk",
                                "minecraft:bogged",
                                "minecraft:ravager"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("nether");
        NETHER_PACK_MOBS = b
                .comment("Entity types in the 'DNA Booster Pack (Nether)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:blaze\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:blaze",
                                "minecraft:ghast",
                                "minecraft:magma_cube",
                                "minecraft:piglin",
                                "minecraft:wither_skeleton",
                                "minecraft:zombified_piglin",
                                "minecraft:zoglin",
                                "minecraft:hoglin"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("rarePassive");
        RARE_PASSIVE_PACK_MOBS = b
                .comment("Entity types in the 'DNA Collector Pack (Rare/Passive)' sample.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:goat\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:goat",
                                "minecraft:armadillo",
                                "minecraft:donkey",
                                "minecraft:horse",
                                "minecraft:turtle"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("baby");
        BABY_PACK_MOBS = b
                .comment("Entity types in the 'DNA Booster Pack (Baby)' sample.",
                         "Every mob spawned from this pack is forced into its baby form.",
                         "Pre-calculated from all mobs confirmed to have baby variants in 26.1.")
                .defineListAllowEmpty("mobs",
                        List.of(
                                // Animals
                                "minecraft:armadillo",
                                "minecraft:axolotl",
                                "minecraft:bee",
                                "minecraft:camel",
                                "minecraft:camel_husk",
                                "minecraft:cat",
                                "minecraft:chicken",
                                "minecraft:cow",
                                "minecraft:dolphin",
                                "minecraft:donkey",
                                "minecraft:fox",
                                "minecraft:goat",
                                "minecraft:happy_ghast",
                                "minecraft:horse",
                                "minecraft:llama",
                                "minecraft:mooshroom",
                                "minecraft:mule",
                                "minecraft:nautilus",
                                "minecraft:ocelot",
                                "minecraft:panda",
                                "minecraft:pig",
                                "minecraft:polar_bear",
                                "minecraft:rabbit",
                                "minecraft:sheep",
                                "minecraft:sniffer",
                                "minecraft:squid",
                                "minecraft:glow_squid",
                                "minecraft:strider",
                                "minecraft:trader_llama",
                                "minecraft:turtle",
                                "minecraft:villager",
                                "minecraft:wolf",
                                // Undead / monster variants
                                "minecraft:hoglin",
                                "minecraft:husk",
                                "minecraft:piglin",
                                "minecraft:skeleton_horse",
                                "minecraft:zombie",
                                "minecraft:zombie_horse",
                                "minecraft:zombie_nautilus",
                                "minecraft:zombie_villager",
                                "minecraft:zombified_piglin",
                                "minecraft:zoglin"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.push("wrongMobs");
        WRONG_MOBS_PACK_MOBS = b
                .comment("Entity types in the 'DNA Booster Pack (Wrong Mobs Only)' sample.",
                         "Mobs with baby variants are forced to spawn as adults.",
                         "Each entry must be a valid entity type ID, e.g. \"minecraft:allay\".")
                .defineListAllowEmpty("mobs",
                        List.of(
                                "minecraft:allay",
                                "minecraft:axolotl",
                                "minecraft:bat",
                                "minecraft:bee",
                                "minecraft:cat",
                                "minecraft:copper_golem",
                                "minecraft:dolphin",
                                "minecraft:fox",
                                "minecraft:frog",
                                "minecraft:ocelot",
                                "minecraft:panda",
                                "minecraft:parrot",
                                "minecraft:polar_bear",
                                "minecraft:sniffer",
                                "minecraft:snow_golem",
                                "minecraft:strider",
                                "minecraft:villager",
                                "minecraft:wolf"
                        ),
                        () -> "",
                        e -> e instanceof String);
        b.pop();

        b.pop(); // dnaSamplePacks

        SPEC = b.build();
    }

    private MGRServerConfig() {}
}
