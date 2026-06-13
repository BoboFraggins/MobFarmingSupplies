package net.bobofraggins.mobfarmingsupplies.loot;

import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.MGRConfig;
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

    public static void register() {
        LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {
            String tableId = id.toString();

            float commonChance = (float) MGRConfig.getDnaSamplePackCommonChestChance();
            float rareChance = (float) MGRConfig.getDnaSamplePackRareChestChance();

            if (ChestLootTables.OVERWORLD_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_rare", commonChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_baby", commonChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_passive_rare", commonChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_wrong", commonChance);
                if (Platform.isModLoaded("aquaculture"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_aquaculture", rareChance);
                if (Platform.isModLoaded("evilcraft"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_evilcraft", commonChance);
            }

            if (ChestLootTables.NETHER_END_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_rare", rareChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_baby", rareChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_passive_rare", rareChance);
                addItem(tableBuilder, "mobfarmingsupplies:dna_sample_wrong", rareChance);
                if (Platform.isModLoaded("evilcraft"))
                    addItem(tableBuilder, "mobfarmingsupplies:dna_booster_pack_evilcraft", rareChance);
            }

            if (ChestLootTables.OVERWORLD_CHESTS.contains(tableId) || ChestLootTables.NETHER_END_CHESTS.contains(tableId)) {
                float toggleButtonChance = (float) MGRConfig.getToggleButtonChestChance();
                addItem(tableBuilder, "mobfarmingsupplies:red_alert_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:dramatic_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:rimshot_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:wilhelm_button", toggleButtonChance);
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
