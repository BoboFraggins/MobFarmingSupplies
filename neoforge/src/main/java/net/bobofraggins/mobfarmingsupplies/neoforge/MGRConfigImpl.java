package net.bobofraggins.mobfarmingsupplies.neoforge;

import net.bobofraggins.mobfarmingsupplies.MGRServerConfig;

import java.util.List;

public final class MGRConfigImpl {

    public static boolean getFanStrongerBlades() {
        return MGRServerConfig.FAN_STRONGER_BLADES.get();
    }

    public static int getHarvesterMaxUpgrade() {
        return MGRServerConfig.HARVESTER_MAX_UPGRADE.get();
    }

    public static int getCloneOMaticSpawnInterval() {
        return MGRServerConfig.CLONE_O_MATIC_SPAWN_INTERVAL.get();
    }

    public static List<String> getCommonHostilePackMobs() {
        return List.copyOf(MGRServerConfig.COMMON_HOSTILE_PACK_MOBS.get());
    }

    public static List<String> getCommonPassivePackMobs() {
        return List.copyOf(MGRServerConfig.COMMON_PASSIVE_PACK_MOBS.get());
    }

    public static List<String> getAquaticPackMobs() {
        return List.copyOf(MGRServerConfig.AQUATIC_PACK_MOBS.get());
    }

    public static List<String> getNetherPackMobs() {
        return List.copyOf(MGRServerConfig.NETHER_PACK_MOBS.get());
    }

    public static List<String> getRareHostilePackMobs() {
        return List.copyOf(MGRServerConfig.RARE_HOSTILE_PACK_MOBS.get());
    }

    public static List<String> getRarePassivePackMobs() {
        return List.copyOf(MGRServerConfig.RARE_PASSIVE_PACK_MOBS.get());
    }

    public static List<String> getBabyPackMobs() {
        return List.copyOf(MGRServerConfig.BABY_PACK_MOBS.get());
    }

    public static List<String> getWrongMobsPackMobs() {
        return List.copyOf(MGRServerConfig.WRONG_MOBS_PACK_MOBS.get());
    }

    public static double getToggleButtonChestChance() {
        return MGRServerConfig.TOGGLE_BUTTON_CHEST_CHANCE.get();
    }

    public static double getDnaSamplePackCommonChestChance() {
        return MGRServerConfig.DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE.get();
    }

    public static double getDnaSamplePackRareChestChance() {
        return MGRServerConfig.DNA_SAMPLE_PACK_RARE_CHEST_CHANCE.get();
    }
}
