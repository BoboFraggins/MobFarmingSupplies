package net.bobofraggins.mobfarmingsupplies.mobhead;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
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
            EntityType.ZOMBIE, Items.ZOMBIE_HEAD,
            EntityType.SKELETON, Items.SKELETON_SKULL,
            EntityType.CREEPER, Items.CREEPER_HEAD,
            EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL,
            EntityType.PIGLIN, Items.PIGLIN_HEAD,
            EntityType.ENDER_DRAGON, Items.DRAGON_HEAD);

    /**
     * Mobs given a Mob Head by this mod: those whose model has a {@code head} part that looks right
     * drawn on its own with the mob's base texture. Ids, so a mob missing from a version is skipped.
     */
    private static final List<String> MOD_HEAD_IDS = List.of(
            // Farm and wild animals
            "pig", "cow", "chicken", "goat", "llama", "trader_llama", "horse", "donkey", "mule",
            "skeleton_horse", "zombie_horse", "camel", "panda", "polar_bear", "wolf", "fox", "cat",
            "ocelot", "rabbit", "parrot", "bat", "armadillo", "axolotl", "frog", "turtle", "dolphin",
            "sniffer",
            // Hostile and neutral mobs
            "spider", "cave_spider", "blaze", "breeze", "warden", "hoglin", "zoglin", "ravager",
            "shulker", "creaking", "phantom", "guardian", "elder_guardian", "husk", "stray", "bogged",
            "zombified_piglin", "piglin_brute", "vex",
            // Illagers and villager-likes
            "pillager", "vindicator", "evoker", "illusioner", "witch", "wandering_trader",
            // Golems and helpers
            "iron_golem", "copper_golem", "allay");

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
