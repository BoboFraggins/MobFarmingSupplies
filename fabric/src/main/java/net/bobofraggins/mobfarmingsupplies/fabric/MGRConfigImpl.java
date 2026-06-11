package net.bobofraggins.mobfarmingsupplies.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.MGRConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MGRConfigImpl {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger("MobFarmingSupplies/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean fanStrongerBlades = false;
    private static int harvesterMaxUpgrade = 10;
    private static int cloneOMaticSpawnInterval = 5;
    private static List<String> commonHostilePackMobs = MGRConfig.DEFAULT_COMMON_HOSTILE;
    private static List<String> commonPassivePackMobs = MGRConfig.DEFAULT_COMMON_PASSIVE;
    private static List<String> aquaticPackMobs = MGRConfig.DEFAULT_AQUATIC;
    private static List<String> netherPackMobs = MGRConfig.DEFAULT_NETHER;
    private static List<String> rareHostilePackMobs = MGRConfig.DEFAULT_RARE_HOSTILE;
    private static List<String> rarePassivePackMobs = MGRConfig.DEFAULT_RARE_PASSIVE;
    private static List<String> babyPackMobs = MGRConfig.DEFAULT_BABY;
    private static List<String> wrongMobsPackMobs = MGRConfig.DEFAULT_WRONG_MOBS;

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

            if (root.has("dnaSamplePacks")) {
                JsonObject packs = root.getAsJsonObject("dnaSamplePacks");
                commonHostilePackMobs = readList(packs, "commonHostile", MGRConfig.DEFAULT_COMMON_HOSTILE);
                commonPassivePackMobs = readList(packs, "commonPassive", MGRConfig.DEFAULT_COMMON_PASSIVE);
                aquaticPackMobs       = readList(packs, "aquatic",       MGRConfig.DEFAULT_AQUATIC);
                netherPackMobs        = readList(packs, "nether",        MGRConfig.DEFAULT_NETHER);
                rareHostilePackMobs   = readList(packs, "rareHostile",   MGRConfig.DEFAULT_RARE_HOSTILE);
                rarePassivePackMobs   = readList(packs, "rarePassive",   MGRConfig.DEFAULT_RARE_PASSIVE);
                babyPackMobs          = readList(packs, "baby",          MGRConfig.DEFAULT_BABY);
                wrongMobsPackMobs     = readList(packs, "wrongMobs",     MGRConfig.DEFAULT_WRONG_MOBS);
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

        JsonObject packs = new JsonObject();
        packs.add("commonHostile", toArray(MGRConfig.DEFAULT_COMMON_HOSTILE));
        packs.add("commonPassive", toArray(MGRConfig.DEFAULT_COMMON_PASSIVE));
        packs.add("aquatic",       toArray(MGRConfig.DEFAULT_AQUATIC));
        packs.add("nether",        toArray(MGRConfig.DEFAULT_NETHER));
        packs.add("rareHostile",   toArray(MGRConfig.DEFAULT_RARE_HOSTILE));
        packs.add("rarePassive",   toArray(MGRConfig.DEFAULT_RARE_PASSIVE));
        packs.add("baby",          toArray(MGRConfig.DEFAULT_BABY));
        packs.add("wrongMobs",     toArray(MGRConfig.DEFAULT_WRONG_MOBS));
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
