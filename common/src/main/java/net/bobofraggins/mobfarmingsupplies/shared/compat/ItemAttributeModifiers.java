package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Stand-in for 1.20.5+'s item attribute modifier component. 1.20.1 items report their modifiers from
 * {@code getDefaultAttributeModifiers(slot)}; an item keeps one of these and returns {@link #forSlot}.
 */
public final class ItemAttributeModifiers {

    private record Entry(Attribute attribute, AttributeModifier modifier, EquipmentSlotGroup slots) {}

    private final List<Entry> entries;

    private ItemAttributeModifiers(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** A modifier with a stable UUID made from {@code id} (1.20.5+ names modifiers by id). */
    public static AttributeModifier modifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        UUID uuid = UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
        return new AttributeModifier(uuid, id.toString(), amount, operation);
    }

    public Multimap<Attribute, AttributeModifier> forSlot(EquipmentSlot slot) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> map = ImmutableMultimap.builder();
        for (Entry e : entries) if (e.slots().test(slot)) map.put(e.attribute(), e.modifier());
        return map.build();
    }

    public static final class Builder {
        private final List<Entry> entries = new ArrayList<>();

        public Builder add(Attribute attribute, AttributeModifier modifier, EquipmentSlotGroup slots) {
            entries.add(new Entry(attribute, modifier, slots));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers(entries);
        }
    }
}
