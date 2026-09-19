package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Curio behaviour for the Magic Hat — grants +2 Luck while worn in a Curios slot. */
public class MagicHatCurioIntegration implements ICurioItem {

    public static final MagicHatCurioIntegration INSTANCE = new MagicHatCurioIntegration();

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, Identifier id, ItemStack stack) {
        return ImmutableMultimap.of(
                Attributes.LUCK,
                new AttributeModifier(MagicHatItem.LUCK_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }
}
