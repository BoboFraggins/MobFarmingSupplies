package net.bobofraggins.mobfarmingsupplies.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MFSConfigImpl {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger("MobFarmingSupplies/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean fanStrongerBlades = false;
    private static int harvesterMaxUpgrade = 10;
    private static int cloneOMaticSpawnInterval = 5;
    private static int tankBaseCapacity = MFSConfig.DEFAULT_TANK_BASE_CAPACITY;
    private static int tankUpgradeMultiplier = MFSConfig.DEFAULT_TANK_UPGRADE_MULTIPLIER;
    private static boolean magicHatZombiesWearHats = true;
    private static boolean presentEndermenCarryPresents = true;
    private static List<String> commonHostilePackMobs = MFSConfig.DEFAULT_COMMON_HOSTILE;
    private static List<String> commonPassivePackMobs = MFSConfig.DEFAULT_COMMON_PASSIVE;
    private static List<String> aquaticPackMobs = MFSConfig.DEFAULT_AQUATIC;
    private static List<String> netherPackMobs = MFSConfig.DEFAULT_NETHER;
    private static List<String> rareHostilePackMobs = MFSConfig.DEFAULT_RARE_HOSTILE;
    private static List<String> rarePassivePackMobs = MFSConfig.DEFAULT_RARE_PASSIVE;
    private static List<String> babyPackMobs = MFSConfig.DEFAULT_BABY;
    private static List<String> wrongMobsPackMobs = MFSConfig.DEFAULT_WRONG_MOBS;
    private static List<String> biomeSpawnDenyList = MFSConfig.DEFAULT_BIOME_SPAWN_DENY_LIST;
    private static double toggleButtonChestChance = MFSConfig.DEFAULT_TOGGLE_BUTTON_CHEST_CHANCE;
    private static double magicHatChestChance = MFSConfig.DEFAULT_MAGIC_HAT_CHEST_CHANCE;
    private static double smoreChestChance = MFSConfig.DEFAULT_SMORE_CHEST_CHANCE;
    private static int hopperItemsPerTransfer = MFSConfig.DEFAULT_HOPPER_ITEMS_PER_TRANSFER;
    private static int hopperTransferInterval = MFSConfig.DEFAULT_HOPPER_TRANSFER_INTERVAL;
    private static int hopperFluidPerTick = MFSConfig.DEFAULT_HOPPER_FLUID_PER_TICK;
    private static double dnaSamplePackCommonChestChance = MFSConfig.DEFAULT_DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE;
    private static double dnaSamplePackRareChestChance = MFSConfig.DEFAULT_DNA_SAMPLE_PACK_RARE_CHEST_CHANCE;

    // ── @ExpectPlatform targets ───────────────────────────────────────────────────

    public static boolean getFanStrongerBlades()         { return fanStrongerBlades; }
    public static int getHarvesterMaxUpgrade()           { return harvesterMaxUpgrade; }
    public static int getCloneOMaticSpawnInterval()      { return cloneOMaticSpawnInterval; }
    public static List<String> getCommonHostilePackMobs() { return commonHostilePackMobs; }
    public static List<String> getCommonPassivePackMobs() { return commonPassivePackMobs; }
    public static List<String> getAquaticPackMobs()      { return aquaticPackMobs; }
    public static List<String> getNetherPackMobs()       { return netherPackMobs; }
    public static List<String> getRareHostilePackMobs()  { return rareHostilePackMobs; }
    public static List<String> getRarePassivePackMobs()  { return rarePassivePackMobs; }
    public static List<String> getBabyPackMobs()         { return babyPackMobs; }
    public static List<String> getWrongMobsPackMobs()    { return wrongMobsPackMobs; }
    public static List<String> getBiomeSpawnDenyList()   { return biomeSpawnDenyList; }
    public static double getToggleButtonChestChance()    { return toggleButtonChestChance; }
    public static double getMagicHatChestChance()        { return magicHatChestChance; }
    public static double getSmoreChestChance()           { return smoreChestChance; }
    public static boolean getMagicHatZombiesWearHats()     { return magicHatZombiesWearHats; }
    public static boolean getPresentEndermenCarryPresents() { return presentEndermenCarryPresents; }
    public static int getTankBaseCapacity()              { return tankBaseCapacity; }
    public static int getTankUpgradeMultiplier()         { return tankUpgradeMultiplier; }
    public static int getHopperItemsPerTransfer()    { return hopperItemsPerTransfer; }
    public static int getHopperTransferInterval()    { return hopperTransferInterval; }
    public static int getHopperFluidPerTick()        { return hopperFluidPerTick; }
    // Energy and chemicals aren't moved on Fabric (no energy API in Fabric itself, no Mekanism).
    public static int getHopperEnergyPerTick()       { return 0; }
    public static int getHopperChemicalPerTick()     { return 0; }
    public static double getDnaSamplePackCommonChestChance() { return dnaSamplePackCommonChestChance; }
    public static double getDnaSamplePackRareChestChance()   { return dnaSamplePackRareChestChance; }

    // ── Loading ───────────────────────────────────────────────────────────────────

    public static void load() {
        Path configFile = Platform.getConfigFolder().resolve("mobfarmingsupplies-server.json");
        if (!Files.exists(configFile)) {
            saveDefaults(configFile);
            return;
        }
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                LOGGER.warn("mobfarmingsupplies-server.json is empty or malformed — using defaults");
                return;
            }

            if (root.has("fan")) {
                JsonObject fan = root.getAsJsonObject("fan");
                if (fan.has("strongerBlades"))
                    fanStrongerBlades = fan.get("strongerBlades").getAsBoolean();
            }

            if (root.has("mobHarvester")) {
                JsonObject mh = root.getAsJsonObject("mobHarvester");
                if (mh.has("maxUpgrade"))
                    harvesterMaxUpgrade = clamp(mh.get("maxUpgrade").getAsInt(), 0, 10);
            }

            if (root.has("cloneOMatic")) {
                JsonObject com = root.getAsJsonObject("cloneOMatic");
                if (com.has("spawnInterval"))
                    cloneOMaticSpawnInterval = clamp(com.get("spawnInterval").getAsInt(), 1, 200);
            }

            if (root.has("tank")) {
                JsonObject tank = root.getAsJsonObject("tank");
                if (tank.has("baseCapacity"))
                    tankBaseCapacity = clamp(tank.get("baseCapacity").getAsInt(), 1, 2_000_000);
                if (tank.has("upgradeMultiplier"))
                    tankUpgradeMultiplier = clamp(tank.get("upgradeMultiplier").getAsInt(), 1, 16);
            }

            if (root.has("toggleButtons")) {
                JsonObject tb = root.getAsJsonObject("toggleButtons");
                if (tb.has("chestDropChance"))
                    toggleButtonChestChance = clamp(tb.get("chestDropChance").getAsDouble(), 0.0, 1.0);
            }

            if (root.has("magicHat")) {
                JsonObject mhc = root.getAsJsonObject("magicHat");
                if (mhc.has("chestDropChance"))
                    magicHatChestChance = clamp(mhc.get("chestDropChance").getAsDouble(), 0.0, 1.0);
                if (mhc.has("zombiesWearHats"))
                    magicHatZombiesWearHats = mhc.get("zombiesWearHats").getAsBoolean();
            }

            if (root.has("present")) {
                JsonObject pr = root.getAsJsonObject("present");
                if (pr.has("endermenCarryPresents"))
                    presentEndermenCarryPresents = pr.get("endermenCarryPresents").getAsBoolean();
            }

            if (root.has("smores")) {
                JsonObject sm = root.getAsJsonObject("smores");
                if (sm.has("chestDropChance"))
                    smoreChestChance = clamp(sm.get("chestDropChance").getAsDouble(), 0.0, 1.0);
            }

            // "hoppers" covers all four hoppers; older files called the section "omnidirectionalHopper".
            String hopperKey = root.has("hoppers") ? "hoppers" : "omnidirectionalHopper";
            if (root.has(hopperKey)) {
                JsonObject oh = root.getAsJsonObject(hopperKey);
                if (oh.has("itemsPerTransfer"))
                    hopperItemsPerTransfer = clamp(oh.get("itemsPerTransfer").getAsInt(), 1, 4096);
                if (oh.has("itemTransferIntervalTicks"))
                    hopperTransferInterval = clamp(oh.get("itemTransferIntervalTicks").getAsInt(), 1, 200);
                if (oh.has("fluidPerTick"))
                    hopperFluidPerTick = clamp(oh.get("fluidPerTick").getAsInt(), 1, Integer.MAX_VALUE);
            }

            if (root.has("dnaSamplePacks")) {
                JsonObject packs = root.getAsJsonObject("dnaSamplePacks");
                commonHostilePackMobs = readList(packs, "commonHostile", MFSConfig.DEFAULT_COMMON_HOSTILE);
                commonPassivePackMobs = readList(packs, "commonPassive", MFSConfig.DEFAULT_COMMON_PASSIVE);
                aquaticPackMobs       = readList(packs, "aquatic",       MFSConfig.DEFAULT_AQUATIC);
                netherPackMobs        = readList(packs, "nether",        MFSConfig.DEFAULT_NETHER);
                rareHostilePackMobs   = readList(packs, "rareHostile",   MFSConfig.DEFAULT_RARE_HOSTILE);
                rarePassivePackMobs   = readList(packs, "rarePassive",   MFSConfig.DEFAULT_RARE_PASSIVE);
                babyPackMobs          = readList(packs, "baby",          MFSConfig.DEFAULT_BABY);
                wrongMobsPackMobs     = readList(packs, "wrongMobs",     MFSConfig.DEFAULT_WRONG_MOBS);
                biomeSpawnDenyList    = readList(packs, "biomeSpawnDenyList", MFSConfig.DEFAULT_BIOME_SPAWN_DENY_LIST);

                if (packs.has("chestLoot")) {
                    JsonObject chestLoot = packs.getAsJsonObject("chestLoot");
                    if (chestLoot.has("commonChestChance"))
                        dnaSamplePackCommonChestChance = clamp(chestLoot.get("commonChestChance").getAsDouble(), 0.0, 1.0);
                    if (chestLoot.has("rareChestChance"))
                        dnaSamplePackRareChestChance = clamp(chestLoot.get("rareChestChance").getAsDouble(), 0.0, 1.0);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load mobfarmingsupplies-server.json — using defaults", e);
        }
    }

    private static List<String> readList(JsonObject parent, String key, List<String> fallback) {
        if (!parent.has(key)) return fallback;
        JsonArray arr = parent.getAsJsonArray(key);
        if (arr == null) return fallback;
        List<String> result = new ArrayList<>(arr.size());
        for (JsonElement elem : arr) {
            if (elem.isJsonPrimitive()) result.add(elem.getAsString());
        }
        return result;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private static void saveDefaults(Path configFile) {
        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return;
        }

        JsonObject root = new JsonObject();

        JsonObject fan = new JsonObject();
        fan.addProperty("strongerBlades", false);
        root.add("fan", fan);

        JsonObject mh = new JsonObject();
        mh.addProperty("maxUpgrade", 10);
        root.add("mobHarvester", mh);

        JsonObject com = new JsonObject();
        com.addProperty("spawnInterval", 5);
        root.add("cloneOMatic", com);

        JsonObject tank = new JsonObject();
        tank.addProperty("baseCapacity", MFSConfig.DEFAULT_TANK_BASE_CAPACITY);
        tank.addProperty("upgradeMultiplier", MFSConfig.DEFAULT_TANK_UPGRADE_MULTIPLIER);
        root.add("tank", tank);

        JsonObject tb = new JsonObject();
        tb.addProperty("chestDropChance", MFSConfig.DEFAULT_TOGGLE_BUTTON_CHEST_CHANCE);
        root.add("toggleButtons", tb);

        JsonObject mhc = new JsonObject();
        mhc.addProperty("chestDropChance", MFSConfig.DEFAULT_MAGIC_HAT_CHEST_CHANCE);
        mhc.addProperty("zombiesWearHats", true);
        root.add("magicHat", mhc);

        JsonObject pr = new JsonObject();
        pr.addProperty("endermenCarryPresents", true);
        root.add("present", pr);

        JsonObject sm = new JsonObject();
        sm.addProperty("chestDropChance", MFSConfig.DEFAULT_SMORE_CHEST_CHANCE);
        root.add("smores", sm);

        JsonObject oh = new JsonObject();
        oh.addProperty("itemsPerTransfer", MFSConfig.DEFAULT_HOPPER_ITEMS_PER_TRANSFER);
        oh.addProperty("itemTransferIntervalTicks", MFSConfig.DEFAULT_HOPPER_TRANSFER_INTERVAL);
        oh.addProperty("fluidPerTick", MFSConfig.DEFAULT_HOPPER_FLUID_PER_TICK);
        root.add("hoppers", oh);

        JsonObject packs = new JsonObject();
        packs.add("commonHostile", toArray(MFSConfig.DEFAULT_COMMON_HOSTILE));
        packs.add("commonPassive", toArray(MFSConfig.DEFAULT_COMMON_PASSIVE));
        packs.add("aquatic",       toArray(MFSConfig.DEFAULT_AQUATIC));
        packs.add("nether",        toArray(MFSConfig.DEFAULT_NETHER));
        packs.add("rareHostile",   toArray(MFSConfig.DEFAULT_RARE_HOSTILE));
        packs.add("rarePassive",   toArray(MFSConfig.DEFAULT_RARE_PASSIVE));
        packs.add("baby",          toArray(MFSConfig.DEFAULT_BABY));
        packs.add("wrongMobs",     toArray(MFSConfig.DEFAULT_WRONG_MOBS));
        packs.add("biomeSpawnDenyList", toArray(MFSConfig.DEFAULT_BIOME_SPAWN_DENY_LIST));

        JsonObject chestLoot = new JsonObject();
        chestLoot.addProperty("commonChestChance", MFSConfig.DEFAULT_DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE);
        chestLoot.addProperty("rareChestChance", MFSConfig.DEFAULT_DNA_SAMPLE_PACK_RARE_CHEST_CHANCE);
        packs.add("chestLoot", chestLoot);

        root.add("dnaSamplePacks", packs);

        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
            LOGGER.info("Wrote default config to {}", configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to write default config file", e);
        }
    }

    private static JsonArray toArray(List<String> list) {
        JsonArray arr = new JsonArray();
        for (String s : list) arr.add(s);
        return arr;
    }
}
