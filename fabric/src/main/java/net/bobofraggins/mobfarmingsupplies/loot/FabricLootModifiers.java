package net.bobofraggins.mobfarmingsupplies.loot;

import dev.architectury.platform.Platform;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

/**
 * Fabric equivalent of the NeoForge global loot modifier system.
 * Uses {@code LootTableEvents.MODIFY} to inject DNA sample pack items into
 * vanilla and mod-compat chest loot tables, mirroring the behaviour defined
 * by the JSON files under {@code neoforge/resources/data/…/loot_modifiers/}.
 *
 * <p>Items are resolved lazily at loot-table-load time; missing items (e.g.
 * DNA sample packs not yet registered pending Phase 9) are silently skipped.
 */
public final class FabricLootModifiers {

    private static final Set<String> OVERWORLD_CHESTS = Set.of(
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

    private static final Set<String> NETHER_END_CHESTS = Set.of(
            "minecraft:chests/nether_bridge",
            "minecraft:chests/bastion_bridge",
            "minecraft:chests/bastion_hoglin_stable",
            "minecraft:chests/bastion_other",
            "minecraft:chests/bastion_treasure",
            "minecraft:chests/end_city_treasure"
    );

    public static void register() {
        LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {
            String tableId = id.toString();

            if (OVERWORLD_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_rare", 0.01f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_baby", 0.01f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_passive_rare", 0.01f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_wrong", 0.01f);
                if (Platform.isModLoaded("aquaculture"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aquaculture", 0.05f);
                if (Platform.isModLoaded("evilcraft"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_evilcraft", 0.01f);
            }

            if (NETHER_END_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_rare", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_baby", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_passive_rare", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_wrong", 0.05f);
                if (Platform.isModLoaded("evilcraft"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_evilcraft", 0.05f);
            }

            if (OVERWORLD_CHESTS.contains(tableId) || NETHER_END_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:red_alert_button", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:dramatic_button", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:rimshot_button", 0.05f);
                addItem(tableBuilder, "mobfarmingsupplies:wilhelm_button", 0.05f);
            }

            if (Platform.isModLoaded("aether_ii")) {
                switch (tableId) {
                    case "aether_ii:chests/dungeons/sentry_ruins/common":
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_passive", 0.01f);
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_hostile", 0.01f);
                        break;
                    case "aether_ii:chests/dungeons/sentry_ruins/rare":
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_passive", 0.03f);
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_hostile", 0.03f);
                        break;
                    case "aether_ii:chests/dungeons/sentry_ruins/boss":
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_passive", 0.05f);
                        addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aether_hostile", 0.05f);
                        break;
                }
            }
        });
    }

    private static void addItem(LootTable.Builder tableBuilder, String itemId, float chance) {
        Identifier rl = Identifier.tryParse(itemId);
        if (rl == null) return;
        Item item = BuiltInRegistries.ITEM.getValue(rl);
        if (item == null || item == Items.AIR) return;
        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .when(LootItemRandomChanceCondition.randomChance(chance))));
    }
}
