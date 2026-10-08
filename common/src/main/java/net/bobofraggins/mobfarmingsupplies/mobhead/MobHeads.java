package net.bobofraggins.mobfarmingsupplies.mobhead;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * Which mobs have a head, and the head item for each: vanilla's own head items where vanilla has
 * one, a player's own head for players, and this mod's Mob Head (drawn from the mob's own model;
 * see {@code MobHeadModels}) for {@link #MOD_HEADS}.
 */
public final class MobHeads {

    /** Mobs with a vanilla head item. */
    private static final Map<EntityType<?>, Item> VANILLA_HEADS = Map.of(
            EntityTypes.ZOMBIE, Items.ZOMBIE_HEAD,
            EntityTypes.SKELETON, Items.SKELETON_SKULL,
            EntityTypes.CREEPER, Items.CREEPER_HEAD,
            EntityTypes.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL,
            EntityTypes.PIGLIN, Items.PIGLIN_HEAD,
            EntityTypes.GIANT, Items.ZOMBIE_HEAD,
            EntityTypes.ENDER_DRAGON, Items.DRAGON_HEAD);

    /**
     * Mobs given a Mob Head by this mod: those whose model has a head part that looks right drawn
     * on its own (with a few extra layers for some; see {@code MobHeadModels}). Ids, so a mob missing
     * from a version is skipped. Kept in alphabetical order, which is the order they're listed in
     * the creative tab and JEI.
     */
    private static final List<String> MOD_HEAD_IDS = List.of(
            "allay", "armadillo", "axolotl", "bat", "blaze", "bogged", "breeze", "camel",
            "camel_husk", "cat", "cave_spider", "chicken", "copper_golem", "cow", "creaking",
            "dolphin", "donkey", "drowned", "elder_guardian", "enderman", "evoker", "fox", "frog",
            "ghast", "goat", "guardian", "happy_ghast", "hoglin", "horse", "husk", "illusioner",
            "iron_golem", "llama", "magma_cube", "mooshroom", "mule", "ocelot", "panda", "parched",
            "parrot", "phantom", "pig", "piglin_brute", "pillager", "polar_bear", "rabbit",
            "ravager", "sheep", "shulker", "skeleton_horse", "slime", "sniffer", "spider", "stray",
            "trader_llama", "turtle", "vex", "villager", "vindicator", "wandering_trader", "warden",
            "witch", "wolf", "zoglin", "zombie_horse", "zombie_villager", "zombified_piglin");

    /** The entity types in {@link #MOD_HEAD_IDS} that exist in this version, in that order. */
    public static final List<EntityType<?>> MOD_HEADS = MOD_HEAD_IDS.stream()
            .map(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(id)))
            .flatMap(Optional::stream)
            .<EntityType<?>>map(t -> t)
            .toList();

    private MobHeads() {}

    /** A Mob Head item for {@code type}. */
    public static ItemStack modHead(EntityType<?> type) {
        ItemStack stack = new ItemStack(Registration.MOB_HEAD_ITEM.get());
        stack.set(Registration.MOB_HEAD_TYPE.get(), type);
        return stack;
    }

    /** The head {@code entity} drops when beheaded, or empty if it has none (babies have none). */
    public static ItemStack headFor(LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(net.minecraft.core.component.DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
            return head;
        }
        if (entity.isBaby()) return ItemStack.EMPTY;
        Item vanilla = VANILLA_HEADS.get(entity.getType());
        if (vanilla != null) return new ItemStack(vanilla);
        return MOD_HEADS.contains(entity.getType()) ? modHead(entity.getType()) : ItemStack.EMPTY;
    }

    /** Every head Beheading can drop: the vanilla mob heads, a player head and every Mob Head. */
    public static List<ItemStack> allHeads() {
        List<ItemStack> heads = new java.util.ArrayList<>();
        for (Item item : List.of(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL,
                Items.CREEPER_HEAD, Items.PIGLIN_HEAD, Items.DRAGON_HEAD, Items.PLAYER_HEAD)) {
            heads.add(new ItemStack(item));
        }
        for (EntityType<?> type : MOD_HEADS) heads.add(modHead(type));
        return heads;
    }
}
