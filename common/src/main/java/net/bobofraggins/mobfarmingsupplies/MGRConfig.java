package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;

/**
 * Cross-platform config accessor for MobFarmingSupplies server settings.
 *
 * <p>On NeoForge the accessors delegate to {@link MGRServerConfig} (loaded via
 * {@code ModConfigSpec} from {@code mobfarmingsupplies-server.toml}).
 * On Fabric they return values loaded from
 * {@code config/mobfarmingsupplies-server.json} at server-start time.
 *
 * <p>All methods return safe defaults until the platform-specific config is loaded.
 */
public final class MGRConfig {

    private MGRConfig() {}

    // ── Defaults ──────────────────────────────────────────────────────────────────
    // These are the canonical defaults; both the NeoForge TOML spec and the Fabric
    // JSON file use these values as their starting point.

    public static final List<String> DEFAULT_COMMON_HOSTILE = List.of(
            "minecraft:zombie", "minecraft:zombie_villager", "minecraft:skeleton",
            "minecraft:enderman", "minecraft:spider", "minecraft:slime",
            "minecraft:creeper", "minecraft:witch");

    public static final List<String> DEFAULT_COMMON_PASSIVE = List.of(
            "minecraft:cow", "minecraft:pig", "minecraft:sheep",
            "minecraft:chicken", "minecraft:mooshroom", "minecraft:rabbit");

    public static final List<String> DEFAULT_AQUATIC = List.of(
            "minecraft:drowned", "minecraft:guardian", "minecraft:squid",
            "minecraft:glow_squid", "minecraft:nautilus", "minecraft:dolphin",
            "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish",
            "minecraft:pufferfish");

    public static final List<String> DEFAULT_RARE_HOSTILE = List.of(
            "minecraft:breeze", "minecraft:evoker", "minecraft:phantom",
            "minecraft:pillager", "minecraft:vindicator", "minecraft:cave_spider",
            "minecraft:husk", "minecraft:bogged", "minecraft:ravager");

    public static final List<String> DEFAULT_NETHER = List.of(
            "minecraft:blaze", "minecraft:ghast", "minecraft:magma_cube",
            "minecraft:piglin", "minecraft:wither_skeleton",
            "minecraft:zombified_piglin", "minecraft:zoglin", "minecraft:hoglin");

    public static final List<String> DEFAULT_RARE_PASSIVE = List.of(
            "minecraft:goat", "minecraft:armadillo", "minecraft:donkey",
            "minecraft:horse", "minecraft:turtle");

    public static final List<String> DEFAULT_BABY = List.of(
            // Animals
            "minecraft:armadillo", "minecraft:axolotl", "minecraft:bee",
            "minecraft:camel", "minecraft:camel_husk", "minecraft:cat",
            "minecraft:chicken", "minecraft:cow", "minecraft:dolphin",
            "minecraft:donkey", "minecraft:fox", "minecraft:goat",
            "minecraft:happy_ghast", "minecraft:horse", "minecraft:llama",
            "minecraft:mooshroom", "minecraft:mule", "minecraft:nautilus",
            "minecraft:ocelot", "minecraft:panda", "minecraft:pig",
            "minecraft:polar_bear", "minecraft:rabbit", "minecraft:sheep",
            "minecraft:sniffer", "minecraft:squid", "minecraft:glow_squid",
            "minecraft:strider", "minecraft:trader_llama", "minecraft:turtle",
            "minecraft:villager", "minecraft:wolf",
            // Undead / monster variants
            "minecraft:hoglin", "minecraft:husk", "minecraft:piglin",
            "minecraft:skeleton_horse", "minecraft:zombie", "minecraft:zombie_horse",
            "minecraft:zombie_nautilus", "minecraft:zombie_villager",
            "minecraft:zombified_piglin", "minecraft:zoglin");

    public static final List<String> DEFAULT_WRONG_MOBS = List.of(
            "minecraft:allay", "minecraft:axolotl", "minecraft:bat",
            "minecraft:bee", "minecraft:cat", "minecraft:copper_golem",
            "minecraft:dolphin", "minecraft:fox", "minecraft:frog",
            "minecraft:ocelot", "minecraft:panda", "minecraft:parrot",
            "minecraft:polar_bear", "minecraft:sniffer", "minecraft:snow_golem",
            "minecraft:strider", "minecraft:villager", "minecraft:wolf");

    public static final double DEFAULT_TOGGLE_BUTTON_CHEST_CHANCE = 0.05;

    /** Default chance for DNA sample/booster packs to appear in "common" tier chests (overworld dungeons/structures). */
    public static final double DEFAULT_DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE = 0.01;

    /** Default chance for DNA sample/booster packs to appear in "rare" tier chests (nether/end structures). */
    public static final double DEFAULT_DNA_SAMPLE_PACK_RARE_CHEST_CHANCE = 0.05;

    // ── Platform-bridged accessors ────────────────────────────────────────────────

    /** Whether the fan's push range is only blocked by solid-collision blocks (default: false). */
    @ExpectPlatform
    public static boolean getFanStrongerBlades() { throw new AssertionError(); }

    /** Maximum upgrade stack size per Mob Harvester slot (0–10, default: 10). */
    @ExpectPlatform
    public static int getHarvesterMaxUpgrade() { throw new AssertionError(); }

    /** Ticks between Clone-O-Matic spawn attempts while powered (1–200, default: 5). */
    @ExpectPlatform
    public static int getCloneOMaticSpawnInterval() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getCommonHostilePackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getCommonPassivePackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getAquaticPackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getNetherPackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getRareHostilePackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getRarePassivePackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getBabyPackMobs() { throw new AssertionError(); }

    @ExpectPlatform
    public static List<String> getWrongMobsPackMobs() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for each toggle button to appear in chest loot tables (default: 0.05). */
    @ExpectPlatform
    public static double getToggleButtonChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for DNA sample/booster packs to appear in "common" tier chests (default: 0.01). */
    @ExpectPlatform
    public static double getDnaSamplePackCommonChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for DNA sample/booster packs to appear in "rare" tier chests (default: 0.05). */
    @ExpectPlatform
    public static double getDnaSamplePackRareChestChance() { throw new AssertionError(); }
}
