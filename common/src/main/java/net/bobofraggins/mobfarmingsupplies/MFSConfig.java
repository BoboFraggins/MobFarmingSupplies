package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;

/**
 * Cross-platform config accessor for MobFarmingSupplies server settings.
 *
 * <p>On NeoForge the accessors delegate to {@link MFSServerConfig} (loaded via
 * {@code ModConfigSpec} from {@code mobfarmingsupplies-server.toml}).
 * On Fabric they return values loaded from
 * {@code config/mobfarmingsupplies-server.json} at server-start time.
 *
 * <p>All methods return safe defaults until the platform-specific config is loaded.
 */
public final class MFSConfig {

    private MFSConfig() {}

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

    /**
     * Entity types never taken from a biome's natural spawns by the Common packs. Entries are
     * entity type IDs, or {@code "modid:*"} for every mob from a mod. Empty by default.
     */
    public static final List<String> DEFAULT_BIOME_SPAWN_DENY_LIST = List.of();

    public static final double DEFAULT_TOGGLE_BUTTON_CHEST_CHANCE = 0.05;

    /** Default chance for DNA sample/booster packs to appear in "common" tier chests (overworld dungeons/structures). */
    public static final double DEFAULT_DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE = 0.01;

    /** Default chance for DNA sample/booster packs to appear in "rare" tier chests (nether/end structures). */
    public static final double DEFAULT_DNA_SAMPLE_PACK_RARE_CHEST_CHANCE = 0.05;

    /** Default chance for the Magic Hat to appear in chest loot tables. */
    public static final double DEFAULT_MAGIC_HAT_CHEST_CHANCE = 0.02;

    /** Default chance for S'mores (1-3) to appear in village house chests. */
    public static final double DEFAULT_SMORE_CHEST_CHANCE = 0.25;

    /** Omnidirectional Hopper defaults: items per INPUT side per transfer, and ticks between item transfers. */
    public static final int DEFAULT_OMNI_HOPPER_ITEMS_PER_TRANSFER = 64;
    public static final int DEFAULT_OMNI_HOPPER_TRANSFER_INTERVAL = 8;
    /** Omnidirectional Hopper defaults per INPUT side per tick: fluid (mB), energy (FE), chemicals (mB). */
    public static final int DEFAULT_OMNI_HOPPER_FLUID_PER_TICK = 1000;
    public static final int DEFAULT_OMNI_HOPPER_ENERGY_PER_TICK = 10000;
    public static final int DEFAULT_OMNI_HOPPER_CHEMICAL_PER_TICK = 1000;

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

    @ExpectPlatform
    public static List<String> getBiomeSpawnDenyList() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for each toggle button to appear in chest loot tables (default: 0.05). */
    @ExpectPlatform
    public static double getToggleButtonChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for DNA sample/booster packs to appear in "common" tier chests (default: 0.01). */
    @ExpectPlatform
    public static double getDnaSamplePackCommonChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for DNA sample/booster packs to appear in "rare" tier chests (default: 0.05). */
    @ExpectPlatform
    public static double getDnaSamplePackRareChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for the Magic Hat to appear in chest loot tables (default: 0.02). */
    @ExpectPlatform
    public static double getMagicHatChestChance() { throw new AssertionError(); }

    /** Chance (0.0–1.0) for 1-3 S'mores to appear in village house chests (default: 0.25). */
    @ExpectPlatform
    public static double getSmoreChestChance() { throw new AssertionError(); }

    /** Items the Omnidirectional Hopper moves per INPUT side per transfer (default: 64). */
    @ExpectPlatform
    public static int getOmniHopperItemsPerTransfer() { throw new AssertionError(); }

    /** Ticks between the Omnidirectional Hopper's item transfers (default: 8). */
    @ExpectPlatform
    public static int getOmniHopperTransferInterval() { throw new AssertionError(); }

    /** Fluid (mB) the Omnidirectional Hopper moves per INPUT side per tick (default: 1000). */
    @ExpectPlatform
    public static int getOmniHopperFluidPerTick() { throw new AssertionError(); }

    /** Energy (FE) the Omnidirectional Hopper moves per INPUT side per tick (default: 10000; NeoForge only). */
    @ExpectPlatform
    public static int getOmniHopperEnergyPerTick() { throw new AssertionError(); }

    /** Mekanism chemicals (mB) the Omnidirectional Hopper moves per INPUT side per tick (default: 1000; NeoForge only). */
    @ExpectPlatform
    public static int getOmniHopperChemicalPerTick() { throw new AssertionError(); }
}
