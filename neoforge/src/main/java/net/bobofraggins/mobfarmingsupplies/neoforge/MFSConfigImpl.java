package net.bobofraggins.mobfarmingsupplies.neoforge;

import net.bobofraggins.mobfarmingsupplies.neoforge.MFSServerConfig;

import java.util.List;

public final class MFSConfigImpl {

    public static boolean getFanStrongerBlades() {
        return MFSServerConfig.FAN_STRONGER_BLADES.get();
    }

    public static int getHarvesterMaxUpgrade() {
        return MFSServerConfig.HARVESTER_MAX_UPGRADE.get();
    }

    public static int getCloneOMaticSpawnInterval() {
        return MFSServerConfig.CLONE_O_MATIC_SPAWN_INTERVAL.get();
    }

    public static List<String> getCommonHostilePackMobs() {
        return List.copyOf(MFSServerConfig.COMMON_HOSTILE_PACK_MOBS.get());
    }

    public static List<String> getCommonPassivePackMobs() {
        return List.copyOf(MFSServerConfig.COMMON_PASSIVE_PACK_MOBS.get());
    }

    public static List<String> getAquaticPackMobs() {
        return List.copyOf(MFSServerConfig.AQUATIC_PACK_MOBS.get());
    }

    public static List<String> getNetherPackMobs() {
        return List.copyOf(MFSServerConfig.NETHER_PACK_MOBS.get());
    }

    public static List<String> getRareHostilePackMobs() {
        return List.copyOf(MFSServerConfig.RARE_HOSTILE_PACK_MOBS.get());
    }

    public static List<String> getRarePassivePackMobs() {
        return List.copyOf(MFSServerConfig.RARE_PASSIVE_PACK_MOBS.get());
    }

    public static List<String> getBabyPackMobs() {
        return List.copyOf(MFSServerConfig.BABY_PACK_MOBS.get());
    }

    public static List<String> getWrongMobsPackMobs() {
        return List.copyOf(MFSServerConfig.WRONG_MOBS_PACK_MOBS.get());
    }

    public static List<String> getBiomeSpawnDenyList() {
        return List.copyOf(MFSServerConfig.BIOME_SPAWN_DENY_LIST.get());
    }

    public static double getToggleButtonChestChance() {
        return MFSServerConfig.TOGGLE_BUTTON_CHEST_CHANCE.get();
    }

    public static double getDnaSamplePackCommonChestChance() {
        return MFSServerConfig.DNA_SAMPLE_PACK_COMMON_CHEST_CHANCE.get();
    }

    public static double getDnaSamplePackRareChestChance() {
        return MFSServerConfig.DNA_SAMPLE_PACK_RARE_CHEST_CHANCE.get();
    }
}
