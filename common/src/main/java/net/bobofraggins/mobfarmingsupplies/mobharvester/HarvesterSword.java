package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemAttributeModifiers;

/**
 * Internal weapon held by the Mob Harvester's fake player during attacks.
 *
 * <p>Not obtainable by players — never appears in the creative tab.
 * Enchantments (Sharpness, Looting) and the Beheading data component are applied
 * dynamically each attack cycle by {@link MobHarvesterBlockEntity}.
 *
 * <p>Extends plain {@link Item} rather than SwordItem so we don't depend on
 * version-specific sword/tier API surface.  The item registers its own attack
 * attribute modifiers directly.
 */
public class HarvesterSword extends Item {

    private static final ResourceLocation DAMAGE_ID =
            new ResourceLocation("mobfarmingsupplies", "harvester_sword_damage");
    private static final ResourceLocation SPEED_ID =
            new ResourceLocation("mobfarmingsupplies", "harvester_sword_distance");

    public HarvesterSword(Properties props) {
        super(props);
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,
            net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        return ATTRIBUTES.forSlot(slot);
    }

    private static final ItemAttributeModifiers ATTRIBUTES = buildAttributes();

    private static ItemAttributeModifiers buildAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        ItemAttributeModifiers.modifier(DAMAGE_ID, 3.0f,
                                AttributeModifier.Operation.ADDITION),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        ItemAttributeModifiers.modifier(SPEED_ID, -2.4f,
                                AttributeModifier.Operation.ADDITION),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
