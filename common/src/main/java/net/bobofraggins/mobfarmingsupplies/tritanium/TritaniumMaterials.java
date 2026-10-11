package net.bobofraggins.mobfarmingsupplies.tritanium;

import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;

/**
 * Tritanium Alloy's tool and armor stats (Tritanium itself makes no gear). They match Mekanism
 * Tools' Osmium: iron mining tier, 1024 durability, speed 4, +4 attack damage, enchantability 14,
 * and armor of 4/8/6/3 with 3 toughness and 0.1 knockback resistance. Each tool's own damage and
 * speed are in Registration.
 */
public final class TritaniumMaterials {

    private TritaniumMaterials() {}

    /** Blocks the Paxel mines quickly: anything a pickaxe, axe, shovel or hoe does. */
    public static final TagKey<Block> MINEABLE_WITH_PAXEL = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "mineable/paxel"));

    /** The Paxel lasts twice as long as the other tools, as Mekanism's do. */
    public static final int PAXEL_DURABILITY = 2048;

    public static final TagKey<Item> ALLOY_REPAIRS = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "repairs_tritanium_alloy"));

    public static final ToolMaterial ALLOY_TOOLS =
            new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1024, 4.0f, 4.0f, 14, ALLOY_REPAIRS);

    public static final ResourceKey<EquipmentAsset> ALLOY_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "tritanium_alloy"));

    /** Durability is 30x vanilla's per-piece base: 330, 480, 450 and 390. */
    public static final ArmorMaterial ALLOY_ARMOR = new ArmorMaterial(30,
            Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3,
                    ArmorType.BODY, 0),
            14, SoundEvents.ARMOR_EQUIP_IRON, 3.0f, 0.1f, ALLOY_REPAIRS, ALLOY_ASSET);
}
