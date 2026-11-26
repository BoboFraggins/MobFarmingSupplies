package net.bobofraggins.mobfarmingsupplies.itemfilter;

import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiPredicate;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

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
            TagKey.create(Registries.ITEM, new ResourceLocation("c", "tools/melee_weapon"));
    private static final TagKey<Item> RANGED_WEAPONS =
            TagKey.create(Registries.ITEM, new ResourceLocation("c", "tools/ranged_weapon"));

    /**
     * 1.20.1 has no {@code c:tools/*_weapon} tags of its own (the mod ships them, see
     * {@code data/c/tags/items/tools}), and modded weapons often join no tag at all, so weapons
     * are also recognised by type.
     */
    private static boolean isWeaponType(ItemStack stack) {
        var item = stack.getItem();
        return item instanceof net.minecraft.world.item.SwordItem
                || item instanceof net.minecraft.world.item.AxeItem
                || item instanceof net.minecraft.world.item.TridentItem
                || item instanceof net.minecraft.world.item.ProjectileWeaponItem;
    }

    public enum Kind implements StringRepresentable {
        EXACT, ITEM, MOD, TAG, PROPERTY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    /**
     * Criteria that aren't expressed as tags. Checks receive the world's registries (may be null
     * if unavailable, in which case registry-based checks don't match).
     */
    public enum Property {
        WEAPON("weapon", (s, r) -> s.is(MELEE_WEAPONS) || s.is(RANGED_WEAPONS) || isWeaponType(s)),
        // Only offered when the item actually carries enchantments (incl. enchanted books).
        ENCHANTED("enchanted", (s, r) -> s.isEnchanted() || !net.minecraft.world.item.EnchantedBookItem.getEnchantments(s).isEmpty()),
        // Asks the enchantments rather than checking the ENCHANTABLE component: some mods (e.g.
        // Apothic Enchanting) give every item a default Enchantable component, which would make
        // everything — even Oak Planks — count as enchantable.
        ENCHANTABLE("enchantable", ItemMatcher::anyEnchantmentSupports),
        DAMAGEABLE("damageable", (s, r) -> s.isDamageableItem()),
        DAMAGED("damaged", (s, r) -> s.isDamaged());

        private final String id;
        private final BiPredicate<ItemStack, HolderLookup.Provider> test;

        Property(String id, BiPredicate<ItemStack, HolderLookup.Provider> test) {
            this.id = id;
            this.test = test;
        }

        public String id() { return id; }

        public boolean test(ItemStack stack, @Nullable HolderLookup.Provider registries) {
            return test.test(stack, registries);
        }

        static Property byId(String id) {
            for (Property p : values()) if (p.id.equals(id)) return p;
            return null;
        }
    }

    public static final Codec<ItemMatcher> CODEC = RecordCodecBuilder.create(i -> i.group(
                    StringRepresentable.fromEnum(Kind::values).fieldOf("kind").forGetter(ItemMatcher::kind),
                    MFSCodecs.OPTIONAL_ITEM_STACK.optionalFieldOf("item", ItemStack.EMPTY).forGetter(ItemMatcher::item),
                    Codec.STRING.optionalFieldOf("key", "").forGetter(ItemMatcher::key))
            .apply(i, ItemMatcher::new));

    public static final StreamCodec<FriendlyByteBuf, ItemMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(ord -> Kind.values()[ord], Kind::ordinal), ItemMatcher::kind,
            MFSStreamCodecs.OPTIONAL_ITEM_STACK, ItemMatcher::item,
            ByteBufCodecs.STRING_UTF8, ItemMatcher::key,
            ItemMatcher::new);

    // ── Factories ─────────────────────────────────────────────────────────────────

    public static ItemMatcher exact(ItemStack stack) { return new ItemMatcher(Kind.EXACT, stack.copyWithCount(1), ""); }

    public static ItemMatcher item(Item item) { return new ItemMatcher(Kind.ITEM, new ItemStack(item), ""); }

    public static ItemMatcher mod(String namespace) { return new ItemMatcher(Kind.MOD, ItemStack.EMPTY, namespace); }

    public static ItemMatcher tag(ResourceLocation tag) { return new ItemMatcher(Kind.TAG, ItemStack.EMPTY, tag.toString()); }

    public static ItemMatcher property(Property p) { return new ItemMatcher(Kind.PROPERTY, ItemStack.EMPTY, p.id()); }

    // ── Matching ──────────────────────────────────────────────────────────────────

    /** @param registries the world's registries, for registry-based properties (may be null) */
    public boolean test(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        if (stack.isEmpty()) return false;
        return switch (kind) {
            case EXACT -> ItemStack.isSameItemSameTags(stack, item);
            case ITEM -> stack.is(item.getItem());
            case MOD -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(key);
            case TAG -> {
                ResourceLocation id = ResourceLocation.tryParse(key);
                yield id != null && stack.is(TagKey.create(Registries.ITEM, id));
            }
            case PROPERTY -> {
                Property p = Property.byId(key);
                yield p != null && p.test(stack, registries);
            }
        };
    }

    /** True if at least one registered enchantment can be applied to {@code stack} (e.g. at an anvil). */
    private static boolean anyEnchantmentSupports(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return BuiltInRegistries.ENCHANTMENT.stream().anyMatch(enchantment -> enchantment.canEnchant(stack));
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
            case ITEM -> item.getItem().getDescription();
            case MOD -> "minecraft".equals(key)
                    ? Component.translatable("filter.mobfarmingsupplies.vanilla")
                    : Component.translatable("filter.mobfarmingsupplies.from",
                            Platform.getOptionalMod(key).map(m -> m.getName()).orElse(key));
            case TAG -> Component.translatable("filter.mobfarmingsupplies.tag." + key.replace(':', '.').replace('/', '.'));
            case PROPERTY -> Component.translatable("filter.mobfarmingsupplies.property." + key);
        };
    }
}
