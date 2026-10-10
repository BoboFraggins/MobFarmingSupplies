package net.bobofraggins.mobfarmingsupplies.loot;

import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

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
            // The event hands over a ResourceKey, whose toString() isn't the plain id.
            String tableId = id.identifier().toString();

            float commonChance = (float) MFSConfig.getDnaSamplePackCommonChestChance();
            float rareChance = (float) MFSConfig.getDnaSamplePackRareChestChance();

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
                float toggleButtonChance = (float) MFSConfig.getToggleButtonChestChance();
                addItem(tableBuilder, "mobfarmingsupplies:red_alert_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:dramatic_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:rimshot_button", toggleButtonChance);
                addItem(tableBuilder, "mobfarmingsupplies:wilhelm_button", toggleButtonChance);

                addItem(tableBuilder, "mobfarmingsupplies:magic_hat", (float) MFSConfig.getMagicHatChestChance());
            }

            if (ChestLootTables.VILLAGE_HOUSE_CHESTS.contains(tableId)) {
                addItem(tableBuilder, "mobfarmingsupplies:smore", (float) MFSConfig.getSmoreChestChance(), 1, 3);
            }

            // Shared inject tables (also used by NeoForge's add_table modifiers): XP Syringes in
            // dungeons, Omnidirectional Hoppers in fortresses, linked bridge pairs in bastions.
            switch (tableId) {
                case "minecraft:chests/simple_dungeon" -> addTable(tableBuilder, registries, "dungeon");
                // The outhouse's guide book only exists (and its table only loads) with Modonomicon.
                case "mobfarmingsupplies:chests/outhouse_shelf" -> {
                    if (Platform.isModLoaded("modonomicon")) addTable(tableBuilder, registries, "outhouse_guide");
                }
                case "minecraft:chests/nether_bridge" -> addTable(tableBuilder, registries, "nether_fortress");
                case "minecraft:chests/bastion_bridge", "minecraft:chests/bastion_hoglin_stable",
                     "minecraft:chests/bastion_other", "minecraft:chests/bastion_treasure" -> addTable(tableBuilder, registries, "bastion");
                default -> { }
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

    /** Adds a pool that rolls {@code mobfarmingsupplies:chests/inject/<name>} once. */
    private static void addTable(LootTable.Builder tableBuilder, HolderLookup.Provider registries, String name) {
        ResourceKey<LootTable> inject = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("mobfarmingsupplies", "chests/inject/" + name));
        registries.lookup(Registries.LOOT_TABLE).flatMap(tables -> tables.get(inject)).ifPresent(table ->
                tableBuilder.withPool(LootPool.lootPool()
                        .setRolls(Holder.direct(new ConstantValue(1)))
                        .add(NestedLootTable.lootTableReference(table))));
    }

    private static void addItem(LootTable.Builder tableBuilder, String itemId, float chance) {
        addItem(tableBuilder, itemId, chance, 1, 1);
    }

    /** Adds a pool giving {@code min}-{@code max} of the item with the given chance. */
    private static void addItem(LootTable.Builder tableBuilder, String itemId, float chance, int min, int max) {
        Identifier rl = Identifier.tryParse(itemId);
        if (rl == null) return;
        Item item = BuiltInRegistries.ITEM.getValue(rl);
        if (item == null || item == Items.AIR) return;
        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(Holder.direct(new ConstantValue(1)))
                .add(LootItem.lootTableItem(item)
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .apply(SetItemCountFunction.setCount(Holder.direct(new UniformGenerator(
                                Holder.direct(new ConstantValue(min)), Holder.direct(new ConstantValue(max))))))));
    }
}
