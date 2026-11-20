package net.bobofraggins.mobfarmingsupplies.oresight;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * What a Bucket of Oresight for a block shows. An ore in one of the shared {@code c:ores/*} tags
 * (which NeoForge and Fabric both provide, and which modded ores usually join) stands for its
 * whole tag, so Gold Ore also finds Deepslate and Nether Gold Ore. Any other block shows only itself.
 */
public final class OresightTargets {

    private OresightTargets() {}

    /** The {@code c:ores/*} tag {@code block} belongs to, if any. */
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct Block tag stream in 26.1.2
    public static Optional<TagKey<Block>> oreTag(Block block) {
        return block.builtInRegistryHolder().tags()
                .filter(tag -> tag.location().getNamespace().equals("c") && tag.location().getPath().startsWith("ores/"))
                .findFirst();
    }

    /** Material tag folders whose names match the ore tags ({@code c:gems/diamond} → {@code c:ores/diamond}). */
    private static final List<String> MATERIAL_FOLDERS = List.of("raw_materials/", "gems/", "dusts/");

    /** Vanilla materials with no material tag of their own, and the ore they come from. */
    private static final Map<Item, String> UNTAGGED_MATERIALS = Map.of(
            Items.NETHERITE_SCRAP, "netherite_scrap",
            Items.COAL, "coal");

    /**
     * The ore a crafting ingredient stands for, or null: an ore block itself, or the first block of
     * the ore tag a material belongs to (Raw Gold, in {@code c:raw_materials/gold}, gives the first of
     * {@code c:ores/gold}). Which of the ore tag's blocks doesn't matter, since Oresight for it
     * traces the whole tag.
     */
    @Nullable
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct Item tag stream in 26.1.2
    public static Block oreFor(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && oreTag(blockItem.getBlock()).isPresent()) {
            return blockItem.getBlock();
        }
        String name = UNTAGGED_MATERIALS.get(stack.getItem());
        if (name == null) {
            name = stack.getItem().builtInRegistryHolder().tags()
                    .filter(tag -> tag.location().getNamespace().equals("c"))
                    .map(tag -> tag.location().getPath())
                    .flatMap(path -> MATERIAL_FOLDERS.stream().filter(path::startsWith).map(f -> path.substring(f.length())))
                    .filter(material -> !material.isEmpty() && !material.contains("/"))
                    .findFirst().orElse(null);
        }
        if (name == null) return null;
        TagKey<Block> ores = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/" + name));
        for (Holder<Block> ore : BuiltInRegistries.BLOCK.getTagOrEmpty(ores)) return ore.value();
        return null;
    }

    /** Which blocks Oresight for {@code block} traces. */
    public static Predicate<BlockState> matcher(Block block) {
        Optional<TagKey<Block>> tag = oreTag(block);
        return tag.<Predicate<BlockState>>map(t -> state -> state.is(t)).orElse(state -> state.is(block));
    }

    /**
     * The name in the bucket's title: the ore tag's own name ("Gold Ores", from the loader's tag
     * translations), or the block's name plus "s" for a modded tag without one, or just the block's name.
     */
    public static Component name(Block block) {
        return oreTag(block).<Component>map(tag -> Component.translatableWithFallback(
                        "tag.block.c." + tag.location().getPath().replace('/', '.'), block.getName().getString() + "s"))
                .orElse(block.getName());
    }
}
