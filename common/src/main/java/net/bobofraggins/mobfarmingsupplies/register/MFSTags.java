package net.bobofraggins.mobfarmingsupplies.register;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys used by Mob Farming Supplies.
 *
 * <p>Data packs (and other mods) can extend these tags by providing their own
 * {@code data/<namespace>/tags/entity_type/no_swab.json} file with
 * {@code "replace": false}.
 */
public final class MFSTags {

    private MFSTags() {}

    public static final class Blocks {

        /**
         * The cross-mod convention tag for blocks that must not be picked up and moved (Carry On,
         * Mekanism's Cardboard Box, ...). A Present won't wrap these. The Present itself is in it,
         * so those mods leave it alone - their pick-up click is the Present's unwrap click.
         */
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
                Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "relocation_not_supported"));

        /**
         * Blocks a Vex can't fly through. Vexes ignore block collision entirely, so these are
         * the only blocks that stop them ({@code EntityVexCollisionMixin}).
         *
         * <p>Defaults: Wither-Proof Glass and Mob Exclusion Glass.
         */
        public static final TagKey<Block> BLOCKS_VEXES = TagKey.create(
                Registries.BLOCK, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "blocks_vexes"));

        private Blocks() {}
    }

    public static final class Items {

        /**
         * What the XP Powered Incubator's sample slot takes: the DNA samples and packs, and the
         * other samples its recipes use (a dragon head). Packs adding incubating recipes with new
         * samples add them here.
         */
        public static final TagKey<Item> INCUBATOR_SAMPLES = TagKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "incubator_samples"));

        /** What the XP Powered Incubator's input slots take: the eggs, and any other recipe inputs. */
        public static final TagKey<Item> INCUBATOR_INPUTS = TagKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "incubator_inputs"));

        private Items() {}
    }

    public static final class EntityTypes {

        /**
         * Entities in this tag cannot be sampled with the DNA Collector.
         *
         * <p>Defaults: {@code minecraft:wither}, {@code minecraft:ender_dragon}.
         * Also includes {@code mob_farming_supplies:no_swab} (optional — ignored when MGU
         * is not installed) so any mob blocked in MGU is automatically blocked here too.
         * Extend via data pack with {@code "replace": false}.
         */
        public static final TagKey<EntityType<?>> NO_DNA_SAMPLING = TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "no_dna_sampling"));

        /**
         * Mobs in this tag never sit down on a Toilet they walk onto (players never do anyway).
         *
         * <p>Defaults: {@code minecraft:shulker}, {@code minecraft:wither}, {@code minecraft:ender_dragon}.
         * Extend via data pack with {@code "replace": false}.
         */
        public static final TagKey<EntityType<?>> CANNOT_USE_TOILET = TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "cannot_use_toilet"));

        private EntityTypes() {}
    }
}
