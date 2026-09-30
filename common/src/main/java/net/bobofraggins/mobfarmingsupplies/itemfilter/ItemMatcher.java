package net.bobofraggins.mobfarmingsupplies.itemfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * One matching criterion an Item Filter can hold.
 *
 * <ul>
 *   <li>{@link Kind#EXACT} — same item and same components as {@code item}
 *   <li>{@link Kind#ITEM} — same item id as {@code item}
 *   <li>{@link Kind#MOD} — item id's namespace equals {@code key} ({@code minecraft} = vanilla)
 *   <li>{@link Kind#TAG} — item is in the item tag {@code key}
 *   <li>{@link Kind#PROPERTY} — item has the {@link Property} named {@code key}
 * </ul>
 *
 * @param item the reference item ({@link ItemStack#EMPTY} for MOD/TAG/PROPERTY)
 * @param key  the namespace, tag id, or property id ("" for EXACT/ITEM)
 */
public record ItemMatcher(Kind kind, ItemStack item, String key) {

    private static final TagKey<Item> MELEE_WEAPONS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/melee_weapon"));
    private static final TagKey<Item> RANGED_WEAPONS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/ranged_weapon"));

    public enum Kind implements StringRepresentable {
        EXACT, ITEM, MOD, TAG, PROPERTY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    /** Criteria that aren't expressed as tags. */
    public enum Property {
        WEAPON("weapon", s -> s.is(MELEE_WEAPONS) || s.is(RANGED_WEAPONS)),
        ENCHANTABLE("enchantable", s -> s.has(DataComponents.ENCHANTABLE)),
        ENCHANTED("enchanted", s -> s.isEnchanted() || s.has(DataComponents.STORED_ENCHANTMENTS)),
        DAMAGEABLE("damageable", ItemStack::isDamageableItem),
        DAMAGED("damaged", ItemStack::isDamaged);

        private final String id;
        private final Predicate<ItemStack> test;

        Property(String id, Predicate<ItemStack> test) {
            this.id = id;
            this.test = test;
        }

        public String id() { return id; }

        public boolean test(ItemStack stack) { return test.test(stack); }

        static Property byId(String id) {
            for (Property p : values()) if (p.id.equals(id)) return p;
            return null;
        }
    }

    public static final Codec<ItemMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
                    StringRepresentable.fromEnum(Kind::values).fieldOf("kind").forGetter(ItemMatcher::kind),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("item", ItemStack.EMPTY).forGetter(ItemMatcher::item),
                    Codec.STRING.optionalFieldOf("key", "").forGetter(ItemMatcher::key))
            .apply(i, ItemMatcher::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(ord -> Kind.values()[ord], Kind::ordinal), ItemMatcher::kind,
            ItemStack.OPTIONAL_STREAM_CODEC, ItemMatcher::item,
            ByteBufCodecs.STRING_UTF8, ItemMatcher::key,
            ItemMatcher::new);

    // ── Factories ─────────────────────────────────────────────────────────────────

    public static ItemMatcher exact(ItemStack stack) { return new ItemMatcher(Kind.EXACT, stack.copyWithCount(1), ""); }

    public static ItemMatcher item(Item item) { return new ItemMatcher(Kind.ITEM, new ItemStack(item), ""); }

    public static ItemMatcher mod(String namespace) { return new ItemMatcher(Kind.MOD, ItemStack.EMPTY, namespace); }

    public static ItemMatcher tag(Identifier tag) { return new ItemMatcher(Kind.TAG, ItemStack.EMPTY, tag.toString()); }

    public static ItemMatcher property(Property p) { return new ItemMatcher(Kind.PROPERTY, ItemStack.EMPTY, p.id()); }

    // ── Matching ──────────────────────────────────────────────────────────────────

    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return switch (kind) {
            case EXACT -> ItemStack.isSameItemSameComponents(stack, item);
            case ITEM -> stack.is(item.getItem());
            case MOD -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(key);
            case TAG -> {
                Identifier id = Identifier.tryParse(key);
                yield id != null && stack.is(TagKey.create(Registries.ITEM, id));
            }
            case PROPERTY -> {
                Property p = Property.byId(key);
                yield p != null && p.test(stack);
            }
        };
    }

    // ── Text ──────────────────────────────────────────────────────────────────────

    /** Row text in the Filter Scribing Terminal's list, e.g. "exactly this item", "a sword". */
    public Component listLabel() {
        return switch (kind) {
            case EXACT -> Component.translatable("filter.mobfarmingsupplies.row.exact");
            case ITEM -> Component.translatable("filter.mobfarmingsupplies.row.similar");
            default -> description();
        };
    }

    /** Phrase used in the filter's name, e.g. "exactly Iron Sword", "from Apotheosis", "a sword". */
    public Component description() {
        return switch (kind) {
            case EXACT -> Component.translatable("filter.mobfarmingsupplies.exact", item.getHoverName());
            case ITEM -> item.getItemName();
            case MOD -> "minecraft".equals(key)
                    ? Component.translatable("filter.mobfarmingsupplies.vanilla")
                    : Component.translatable("filter.mobfarmingsupplies.from",
                            Platform.getOptionalMod(key).map(m -> m.getName()).orElse(key));
            case TAG -> Component.translatable("filter.mobfarmingsupplies.tag." + key.replace(':', '.').replace('/', '.'));
            case PROPERTY -> Component.translatable("filter.mobfarmingsupplies.property." + key);
        };
    }
}
