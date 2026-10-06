package net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric;

import eu.pb4.trinkets.api.event.TrinketEquipmentAttributeModifiersCallback;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Trinkets Updated registration for the Magic Hat's +2 Luck bonus — runs on both the logical
 * client and the dedicated server (attribute modifiers affect server-side Luck-based loot
 * rolls, not just rendering), so this class must stay free of any client-only type.
 *
 * <p>Deliberately isolated in its own class, same reasoning as NeoForge's
 * {@code MagicHatCurioSetup}: Trinkets Updated is a soft (optional) dependency, and any class
 * that is always loaded on Fabric (like {@code MobFarmingSuppliesFabric}, the {@code
 * ModInitializer}) must contain zero symbolic references to a soft dependency's types in any
 * of its methods — loading such a class means the JVM verifies its whole bytecode, throwing
 * {@link NoClassDefFoundError} before an {@code isModLoaded} guard inside it could ever run.
 * Callers must check {@code Platform.isModLoaded("trinkets_updated")} themselves before ever
 * calling {@link #registerCommon()}.
 *
 * <p>The 3D render-layer registration is a <em>separate</em> class again ({@code
 * MagicHatTrinketClientSetup}) — not because of the soft-dependency hazard (this class alone
 * would already be safe either way), but because it references client-only Minecraft/Trinkets
 * rendering types, which a Fabric dedicated server genuinely doesn't have on its classpath at
 * all (unlike NeoForge's merged client+server jar). Mixing that into a class called from the
 * common {@code ModInitializer} would crash every dedicated server, Trinkets installed or not.
 */
public final class MagicHatTrinketSetup {

    private MagicHatTrinketSetup() {}

    public static void registerCommon() {
        TrinketEquipmentAttributeModifiersCallback.EVENT.register((stack, slot, entity, id, consumer) -> {
            if (stack.is(Registration.MAGIC_HAT_ITEM.get())) {
                consumer.accept(
                        Attributes.LUCK,
                        new AttributeModifier(
                                MagicHatItem.LUCK_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
            }
        });
    }
}
