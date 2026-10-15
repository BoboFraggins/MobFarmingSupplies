package net.bobofraggins.mobfarmingsupplies.mobhead;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;

/**
 * The Mob Head item: named after its mob ("Cow Head"); places a standing or wall head. Worn, it
 * protects like an Iron Helmet and takes helmet enchantments (through the
 * {@code enchantable/head_armor} tag), but has no durability so heads still stack.
 */
public class MobHeadItem extends StandingAndWallBlockItem implements Equipable {

    /** An Iron Helmet's armor and toughness. */
    public static final ItemAttributeModifiers ATTRIBUTES = ironHelmet();

    public MobHeadItem(Block standing, Block wall, Properties props) {
        super(standing, wall, props, Direction.DOWN);
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = stack.get(Registration.MOB_HEAD_TYPE.get());
        return type == null ? super.getName(stack)
                : Component.translatable("item.mobfarmingsupplies.mob_head.named", type.getDescription());
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public int getEnchantmentValue() {
        return ArmorMaterials.IRON.value().enchantmentValue();
    }

    private static ItemAttributeModifiers ironHelmet() {
        ArmorMaterial iron = ArmorMaterials.IRON.value();
        ResourceLocation id = ResourceLocation.withDefaultNamespace("armor.helmet");
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(id, iron.getDefense(ArmorItem.Type.HELMET), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(id, iron.toughness(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build();
    }
}
