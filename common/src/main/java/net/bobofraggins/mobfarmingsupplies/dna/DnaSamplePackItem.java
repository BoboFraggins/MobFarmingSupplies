package net.bobofraggins.mobfarmingsupplies.dna;

import net.bobofraggins.mobfarmingsupplies.MGRConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.MobSpawnSettings;

import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A DNA Sample Pack — represents a curated, configurable pool of entity types.
 *
 * <p>Unlike {@link DnaSampleItem}, a pack carries no per-mob NBT data.  Each
 * spawn picks a random entry from the configured entity-type list and creates a
 * fresh entity via the normal spawner rules (random variants, equipment, etc.).
 *
 * <p>The mob list is provided at construction time as a lazy
 * {@code Supplier<List<? extends String>>} so that it is read from the live
 * server config at spawn time rather than at item registration time.
 *
 * <p>When {@code forceBaby} is {@code true} every spawned entity is forced into
 * its baby state.  When {@code false} (the default) every spawned entity is
 * explicitly forced into its adult state, so packs cannot accidentally produce
 * babies via natural-spawning variance.
 *
 * <p>A pack given a {@link MobCategory} (the Common packs) also draws from the natural spawns of
 * that category in the Clone-O-Matic's biome — read from the live biome, so mobs other mods add
 * to it through biome modifiers are included. Those mobs share one pool with the configured
 * list, each equally likely; entries on {@link MGRConfig#getBiomeSpawnDenyList()} are left out.
 */
public class DnaSamplePackItem extends Item implements IDnaSampleItem {

    private final Supplier<List<? extends String>> mobsConfig;
    private final boolean forceBaby;
    @Nullable private final MobCategory biomeCategory;

    /** Constructs a normal (adult-only) pack. */
    public DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig) {
        this(props, mobsConfig, false, null);
    }

    /** Constructs a pack that forces every spawn to baby or adult state. */
    public DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig, boolean forceBaby) {
        this(props, mobsConfig, forceBaby, null);
    }

    /** Constructs an adult-only pack that also spawns the biome's natural {@code biomeCategory} mobs. */
    public DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig, MobCategory biomeCategory) {
        this(props, mobsConfig, false, biomeCategory);
    }

    private DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig, boolean forceBaby,
                              @Nullable MobCategory biomeCategory) {
        super(props);
        this.mobsConfig = mobsConfig;
        this.forceBaby = forceBaby;
        this.biomeCategory = biomeCategory;
    }

    @Override
    @Nullable
    public Entity createSpawnEntity(ItemStack stack, ServerLevel level, BlockPos pos, RandomSource random) {
        List<EntityType<?>> mobs = mobPool(level, pos);
        if (mobs.isEmpty()) return null;

        EntityType<?> type = mobs.get(random.nextInt(mobs.size()));
        Entity entity = type.create(level, EntitySpawnReason.SPAWNER);
        if (entity == null) return null;

        if (forceBaby) {
            applyBabyState(entity, true);
        } else {
            applyBabyState(entity, false);
        }

        return entity;
    }

    /** The configured mobs plus (for a pack with a biome category) the biome's spawns, without duplicates. */
    private List<EntityType<?>> mobPool(ServerLevel level, BlockPos pos) {
        Set<EntityType<?>> pool = new LinkedHashSet<>();
        for (String id : mobsConfig.get()) {
            Identifier loc = Identifier.tryParse(id);
            if (loc != null) BuiltInRegistries.ENTITY_TYPE.getOptional(loc).ifPresent(pool::add);
        }
        if (biomeCategory != null) {
            List<String> deny = MGRConfig.getBiomeSpawnDenyList();
            for (Weighted<MobSpawnSettings.SpawnerData> spawn
                    : level.getBiome(pos).value().getMobSettings().getMobs(biomeCategory).unwrap()) {
                EntityType<?> type = spawn.value().type();
                if (!isDenied(type, deny)) pool.add(type);
            }
        }
        return List.copyOf(pool);
    }

    /** Whether {@code type} matches a deny-list entry: its exact ID, or {@code "modid:*"}. */
    private static boolean isDenied(EntityType<?> type, List<String> deny) {
        if (deny.isEmpty()) return false;
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String id = key.toString();
        String wholeMod = key.getNamespace() + ":*";
        for (String entry : deny) {
            if (entry.equals(id) || entry.equals(wholeMod)) return true;
        }
        return false;
    }

    /**
     * Forces {@code entity} into baby ({@code baby=true}) or adult ({@code baby=false}) state.
     *
     * <p>Three class branches cover every mob that has a baby form in 26.1:
     * <ul>
     *   <li>{@link AgeableMob} — all animals, horses, dolphins, squids, sniffer, happy ghast,
     *       nautilus, strider, villager, hoglin, zoglin, zombie horse, etc.</li>
     *   <li>{@link Zombie} — zombie, husk, zombie villager, zombified piglin.</li>
     *   <li>{@link Piglin} — piglin (AbstractPiglin does not extend AgeableMob).</li>
     * </ul>
     */
    private static void applyBabyState(Entity entity, boolean baby) {
        if (entity instanceof AgeableMob ageable) {
            ageable.setBaby(baby);
        } else if (entity instanceof Zombie zombie) {
            zombie.setBaby(baby);
        } else if (entity instanceof Piglin piglin) {
            piglin.setBaby(baby);
        }
    }
}
