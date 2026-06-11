package net.bobofraggins.mobfarmingsupplies.dna;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.List;
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
 */
public class DnaSamplePackItem extends Item implements IDnaSampleItem {

    private final Supplier<List<? extends String>> mobsConfig;
    private final boolean forceBaby;

    /** Constructs a normal (adult-only) pack. */
    public DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig) {
        this(props, mobsConfig, false);
    }

    /** Constructs a pack that forces every spawn to baby or adult state. */
    public DnaSamplePackItem(Properties props, Supplier<List<? extends String>> mobsConfig, boolean forceBaby) {
        super(props);
        this.mobsConfig = mobsConfig;
        this.forceBaby = forceBaby;
    }

    @Override
    @Nullable
    public Entity createSpawnEntity(ItemStack stack, ServerLevel level, RandomSource random) {
        List<? extends String> mobs = mobsConfig.get();
        if (mobs.isEmpty()) return null;

        String id = mobs.get(random.nextInt(mobs.size()));
        Identifier loc = Identifier.tryParse(id);
        if (loc == null) return null;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(loc).orElse(null);
        if (type == null) return null;

        Entity entity = type.create(level, EntitySpawnReason.SPAWNER);
        if (entity == null) return null;

        if (forceBaby) {
            applyBabyState(entity, true);
        } else {
            applyBabyState(entity, false);
        }

        return entity;
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
