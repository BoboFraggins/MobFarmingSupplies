package net.bobofraggins.mobfarmingsupplies.register;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Tag keys used by Mob Farming Supplies.
 *
 * <p>Data packs (and other mods) can extend these tags by providing their own
 * {@code data/<namespace>/tags/entity_type/no_swab.json} file with
 * {@code "replace": false}.
 */
public final class MGRTags {

    private MGRTags() {}

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

        private EntityTypes() {}
    }
}
