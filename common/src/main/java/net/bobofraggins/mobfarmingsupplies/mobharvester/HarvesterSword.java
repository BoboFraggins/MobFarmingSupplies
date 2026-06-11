package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

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

    private static final Identifier DAMAGE_ID =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "harvester_sword_damage");
    private static final Identifier SPEED_ID =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "harvester_sword_distance");

    public HarvesterSword(Properties props) {
        super(props.attributes(buildAttributes()));
    }

    private static ItemAttributeModifiers buildAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(DAMAGE_ID, 3.0f,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(SPEED_ID, -2.4f,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
