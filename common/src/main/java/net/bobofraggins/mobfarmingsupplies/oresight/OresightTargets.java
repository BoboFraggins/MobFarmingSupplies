package net.bobofraggins.mobfarmingsupplies.oresight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * What Oresight for a block shows. An ore in a per-material ore tag stands for its whole tag, so
 * Gold Ore also finds Deepslate and Nether Gold Ore. Any other block shows only itself.
 *
 * <p>1.20.1 has no shared {@code c:ores/*} tags (those arrived in 1.21), so this reads each
 * loader's own names:
 * <ul>
 *   <li>Forge: {@code forge:ores/gold}, {@code forge:raw_materials/gold}, {@code forge:gems/diamond},
 *       {@code forge:dusts/redstone}
 *   <li>Fabric: {@code c:quartz_ores} (and modded {@code c:tin_ores}), vanilla's {@code minecraft:gold_ores},
 *       {@code c:raw_gold_ores}, {@code c:diamonds}, {@code c:redstone_dusts}
 * </ul>
 * {@code c:ores/*} and friends are still recognised, for mods that already use the newer names.
 */
public final class OresightTargets {

    private OresightTargets() {}

    /** Material tag folders on Forge (and in 1.21-style tags): {@code forge:gems/diamond} → diamond. */
    private static final List<String> MATERIAL_FOLDERS = List.of("raw_materials/", "gems/", "dusts/");

    /** Vanilla materials with no material tag of their own, and the ore they come from. */
    private static final Map<Item, String> UNTAGGED_MATERIALS = Map.of(
            Items.NETHERITE_SCRAP, "netherite_scrap",
            Items.COAL, "coal");

    /** Ores a material comes from when no tag names them (Fabric has no tag for Ancient Debris). */
    private static final Map<Item, Block> UNTAGGED_ORES = Map.of(Items.NETHERITE_SCRAP, Blocks.ANCIENT_DEBRIS);

    /**
     * The material a block tag groups ores of, or null if it isn't a per-material ore tag:
     * {@code forge:ores/gold}, {@code c:ores/gold}, {@code c:gold_ores} or {@code minecraft:gold_ores} → gold.
     */
    @Nullable
    private static String oreMaterial(ResourceLocation tag) {
        String ns = tag.getNamespace(), path = tag.getPath();
        if ((ns.equals("forge") || ns.equals("c")) && path.startsWith("ores/")) {
            String material = path.substring("ores/".length());
            return material.isEmpty() || material.contains("/") ? null : material;
        }
        if ((ns.equals("c") || ns.equals("minecraft")) && path.endsWith("_ores") && !path.contains("/")) {
            String material = path.substring(0, path.length() - "_ores".length());
            return material.isEmpty() || material.startsWith("raw_") ? null : material;
        }
        return null;
    }

    /** Loader tags first, since modded ores join those rather than vanilla's. */
    private static int rank(ResourceLocation tag) {
        return switch (tag.getNamespace()) {
            case "forge" -> 0;
            case "c" -> tag.getPath().startsWith("ores/") ? 1 : 2;
            default -> 3;
        };
    }

    /** The per-material ore tag {@code block} belongs to, if any. */
    @SuppressWarnings("deprecation")
    public static Optional<TagKey<Block>> oreTag(Block block) {
        return block.builtInRegistryHolder().tags()
                .filter(tag -> oreMaterial(tag.location()) != null)
                .min(Comparator.comparingInt(tag -> rank(tag.location())));
    }

    /** The ore tags a material's ores may be in, in preference order. */
    private static List<TagKey<Block>> oreTags(String material) {
        return List.of(
                TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/" + material)),
                TagKey.create(Registries.BLOCK, new ResourceLocation("c", "ores/" + material)),
                TagKey.create(Registries.BLOCK, new ResourceLocation("c", material + "_ores")),
                TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", material + "_ores")));
    }

    /** The materials an item tag may stand for, most likely first. */
    private static List<String> materials(ResourceLocation tag) {
        String ns = tag.getNamespace(), path = tag.getPath();
        List<String> out = new ArrayList<>();
        if (!ns.equals("forge") && !ns.equals("c")) return out;
        for (String folder : MATERIAL_FOLDERS) {
            if (path.startsWith(folder)) out.add(path.substring(folder.length()));
        }
        if (ns.equals("c") && !path.contains("/")) {
            if (path.startsWith("raw_") && path.endsWith("_ores") && path.length() > "raw__ores".length()) {
                out.add(path.substring("raw_".length(), path.length() - "_ores".length())); // c:raw_gold_ores
            } else if (path.endsWith("_dusts")) {
                out.add(path.substring(0, path.length() - "_dusts".length()));             // c:redstone_dusts
            } else {
                // Gems are plural on Fabric: c:diamonds, c:rubies, but c:lapis and c:quartz.
                out.add(path);
                if (path.endsWith("ies")) out.add(path.substring(0, path.length() - 3) + "y");
                if (path.endsWith("es")) out.add(path.substring(0, path.length() - 2));
                if (path.endsWith("s")) out.add(path.substring(0, path.length() - 1));
            }
        }
        out.removeIf(m -> m.isEmpty() || m.contains("/"));
        return out;
    }

    /** The first ore in {@code material}'s ore tag, or null if it has none. */
    @Nullable
    private static Block firstOre(String material) {
        for (TagKey<Block> tag : oreTags(material)) {
            for (Holder<Block> ore : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) return ore.value();
        }
        return null;
    }

    /**
     * The ore a brewing ingredient stands for, or null: an ore block itself, or the first block of
     * the ore tag a material belongs to (Raw Gold gives the first gold ore). Which of the ore tag's
     * blocks doesn't matter, since Oresight for it traces the whole tag.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static Block oreFor(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && (oreTag(blockItem.getBlock()).isPresent()
                || UNTAGGED_ORES.containsValue(blockItem.getBlock()))) {
            return blockItem.getBlock();
        }
        String untagged = UNTAGGED_MATERIALS.get(stack.getItem());
        if (untagged != null) {
            Block ore = firstOre(untagged);
            return ore != null ? ore : UNTAGGED_ORES.get(stack.getItem());
        }
        return stack.getItem().builtInRegistryHolder().tags()
                .flatMap(tag -> materials(tag.location()).stream())
                .map(OresightTargets::firstOre)
                .filter(ore -> ore != null)
                .findFirst().orElse(null);
    }

    /** Which blocks Oresight for {@code block} traces. */
    public static Predicate<BlockState> matcher(Block block) {
        Optional<TagKey<Block>> tag = oreTag(block);
        return tag.<Predicate<BlockState>>map(t -> state -> state.is(t)).orElse(state -> state.is(block));
    }

    /**
     * The name in the potion's title: the ore tag's own name if a translation exists, otherwise the
     * name of the ore it is named after, made plural ("Gold Ores"), or just the block's name.
     */
    public static Component name(Block block) {
        return oreTag(block).<Component>map(tag -> Component.translatableWithFallback(
                        "tag.block." + tag.location().getNamespace() + "." + tag.location().getPath().replace('/', '.'),
                        plural(namesake(tag, block).getName().getString())))
                .orElse(block.getName());
    }

    /**
     * The block that names an ore tag: {@code <material>_ore} (Gold Ore, not Deepslate Gold Ore, which
     * Forge lists first), else the tag's first block, else {@code fallback}.
     */
    private static Block namesake(TagKey<Block> tag, Block fallback) {
        String plain = oreMaterial(tag.location()) + "_ore";
        Block first = null;
        for (Holder<Block> ore : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            if (BuiltInRegistries.BLOCK.getKey(ore.value()).getPath().equals(plain)) return ore.value();
            if (first == null) first = ore.value();
        }
        return first != null ? first : fallback;
    }

    private static String plural(String name) {
        return name.endsWith("s") ? name : name + "s";
    }
}
