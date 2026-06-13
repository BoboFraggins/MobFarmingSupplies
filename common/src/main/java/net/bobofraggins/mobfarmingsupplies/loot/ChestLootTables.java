package net.bobofraggins.mobfarmingsupplies.loot;

import java.util.Set;

/**
 * Shared sets of vanilla chest loot table IDs used to classify chests into
 * "common" (overworld dungeons/structures) and "rare" (nether/end structures)
 * tiers for DNA sample/booster pack chest-loot chances.
 */
public final class ChestLootTables {

    private ChestLootTables() {}

    public static final Set<String> OVERWORLD_CHESTS = Set.of(
            "minecraft:chests/simple_dungeon",
            "minecraft:chests/abandoned_mineshaft",
            "minecraft:chests/pillager_outpost",
            "minecraft:chests/woodland_mansion",
            "minecraft:chests/jungle_temple",
            "minecraft:chests/desert_pyramid",
            "minecraft:chests/stronghold_corridor",
            "minecraft:chests/stronghold_library",
            "minecraft:chests/stronghold_crossing",
            "minecraft:chests/ancient_city",
            "minecraft:chests/trial_chambers/reward",
            "minecraft:chests/trial_chambers/reward_rare",
            "minecraft:chests/ruined_portal",
            "minecraft:chests/igloo_chest",
            "minecraft:chests/shipwreck_treasure"
    );

    public static final Set<String> NETHER_END_CHESTS = Set.of(
            "minecraft:chests/nether_bridge",
            "minecraft:chests/bastion_bridge",
            "minecraft:chests/bastion_hoglin_stable",
            "minecraft:chests/bastion_other",
            "minecraft:chests/bastion_treasure",
            "minecraft:chests/end_city_treasure"
    );
}
