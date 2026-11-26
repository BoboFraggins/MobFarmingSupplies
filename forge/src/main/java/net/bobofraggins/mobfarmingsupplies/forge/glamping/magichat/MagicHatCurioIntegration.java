package net.bobofraggins.mobfarmingsupplies.forge.glamping.magichat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatItem;

/** Curio behaviour for the Magic Hat — grants +2 Luck while worn in a Curios slot. */
public class MagicHatCurioIntegration implements ICurioItem {

    public static final MagicHatCurioIntegration INSTANCE = new MagicHatCurioIntegration();

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, java.util.UUID uuid, ItemStack stack) {
        return ImmutableMultimap.of(
                Attributes.LUCK,
                new AttributeModifier(uuid, MagicHatItem.LUCK_MODIFIER_ID.toString(), 2.0, AttributeModifier.Operation.ADDITION));
    }
}
