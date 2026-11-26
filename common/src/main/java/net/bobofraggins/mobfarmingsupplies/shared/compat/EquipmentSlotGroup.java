package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.entity.EquipmentSlot;

/** Stand-in for 1.20.5+'s slot groups for item attribute modifiers. */
public enum EquipmentSlotGroup {
    ANY, MAINHAND, OFFHAND, HAND, FEET, LEGS, CHEST, HEAD, ARMOR;

    public boolean test(EquipmentSlot slot) {
        return switch (this) {
            case ANY -> true;
            case MAINHAND -> slot == EquipmentSlot.MAINHAND;
            case OFFHAND -> slot == EquipmentSlot.OFFHAND;
            case HAND -> slot.getType() == EquipmentSlot.Type.HAND;
            case FEET -> slot == EquipmentSlot.FEET;
            case LEGS -> slot == EquipmentSlot.LEGS;
            case CHEST -> slot == EquipmentSlot.CHEST;
            case HEAD -> slot == EquipmentSlot.HEAD;
            case ARMOR -> slot.getType() == EquipmentSlot.Type.ARMOR;
        };
    }
}
