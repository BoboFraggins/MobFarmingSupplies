package net.bobofraggins.mobfarmingsupplies.dna;

import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import net.bobofraggins.mobfarmingsupplies.mixin.MobFarmAccessor;
import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * DNA Sample — holds the save data of a specific mob, minus what it carries.
 *
 * <p>Created exclusively by {@link DnaCollectorItem} when right-clicking a
 * non-player {@link net.minecraft.world.entity.LivingEntity}.  The sample
 * carries a {@link DnaSampleContents} data component that stores the entity
 * NBT (including type) and a human-readable mob name used as the item's display
 * name.
 *
 * <p>DNA Samples do not stack (maxStackSize = 1) because each one may represent
 * a distinct mob identity (variant, trades, name, etc.).  Items the mob carried
 * are left out of the sample; clones roll fresh gear the way a natural spawn does.
 *
 * <p>When the Clone-O-Matic's DNA-sample design is finalised, instances of this
 * item placed in its nine DNA slots will define which mob is spawned.
 */
public class DnaSampleItem extends Item implements IDnaSampleItem {

    public DnaSampleItem(Properties props) {
        super(props);
    }

    /**
     * Save keys left out of a sample. The sample keeps what the mob <em>is</em> (type, variant,
     * trades, colour, name, attributes), not what it carries or its momentary state: every clone
     * is built from this NBT, so any item kept here would be copied into every clone and duplicated
     * each time one is killed. Clones get fresh, vanilla-rolled gear at spawn instead
     * (see {@link #createSpawnEntity}).
     */
    private static final List<String> STRIPPED_KEYS = List.of(
            // Carried items: hand/armour/body/saddle slots, villager/piglin/allay inventories,
            // donkey/mule/llama chests, the enderman's block, riders (saved with their own gear).
            "equipment", "drop_chances", "Inventory", "Items", "ChestedHorse", "carriedBlockState", "Passengers",
            // A loot-table override would be copied into every clone.
            "DeathLootTable", "DeathLootTableSeed",
            // Clones must despawn like any other farm mob.
            "PersistenceRequired",
            // A cured villager's discount would carry over to every clone.
            "Gossips",
            // Clones start at full health.
            "Health", "AbsorptionAmount",
            // Anger: neutral mobs keep it in these keys; piglins and hoglins keep it as Brain memories.
            // Brain also holds the original's job site / bed / meeting point, which clones shouldn't inherit.
            "anger_end_time", "angry_at", "Brain");

    /**
     * Returns a copy of {@code entityNbt} without {@link #STRIPPED_KEYS}. Applied when a sample
     * is taken and again at every spawn, so samples made before a key was added are covered too.
     */
    public static CompoundTag stripSampleNbt(CompoundTag entityNbt) {
        CompoundTag stripped = entityNbt.copy();
        STRIPPED_KEYS.forEach(stripped::remove);
        return stripped;
    }

    /**
     * Returns the {@link DnaSampleContents} stored in this stack, or {@code null} if absent.
     * Prefer this helper over accessing the data component type directly to avoid
     * circular import issues.
     */
    @Nullable
    /**
     * A DNA Sample of {@code entity}: its save data (minus carried items and transient state) and
     * its name. Used by the DNA Collector and by the {@code set_dna_sample} loot function.
     */
    public static ItemStack createSample(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.level().registryAccess());
        entity.save(output);
        CompoundTag entityNbt = stripSampleNbt(output.buildResult());
        String mobName = entity.hasCustomName()
                ? entity.getCustomName().getString()
                : entity.getType().getDescription().getString();
        ItemStack sample = new ItemStack(Registration.DNA_SAMPLE.get());
        sample.set(Registration.DNA_SAMPLE_CONTENTS.get(), new DnaSampleContents(entityNbt, mobName));
        return sample;
    }

    public static DnaSampleContents getContents(ItemStack stack) {
        return stack.get(Registration.DNA_SAMPLE_CONTENTS.get());
    }

    @Override
    @Nullable
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct EntityType.is(TagKey) in 26.1.2
    public Entity createSpawnEntity(ItemStack stack, ServerLevel level, BlockPos pos, RandomSource random) {
        DnaSampleContents contents = getContents(stack);
        if (contents == null) return null;
        Entity entity = EntityType.loadEntityRecursive(
                stripSampleNbt(contents.entityNbt()), level, EntitySpawnReason.SPAWNER, EntityProcessor.NOP);
        if (entity == null) return null;
        // Samples taken before a mob was denied sampling must not keep cloning it.
        if (entity.getType().builtInRegistryHolder().is(MFSTags.EntityTypes.NO_DNA_SAMPLING)) return null;

        // The saved NBT carries the original captured mob's UUID. Every clone made
        // from this sample would otherwise share that UUID, and the level refuses to
        // add an entity whose UUID is already in use - so only one clone could ever
        // exist at a time. Give each spawned clone its own identity.
        entity.setUUID(Mth.createInsecureUUID(random));

        // The spawn NBT holds no equipment (see STRIPPED_KEYS). Roll gear the way a natural
        // spawn does - wither skeletons get their sword, skeletons their bow - without the rest
        // of finalizeSpawn, which would re-roll the identity the sample exists to keep.
        if (entity instanceof Mob mob) {
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
            MobFarmAccessor acc = (MobFarmAccessor) mob;
            acc.invokePopulateDefaultEquipmentSlots(random, difficulty);
            acc.invokePopulateDefaultEquipmentEnchantments(level, random, difficulty);
        }
        return entity;
    }

    /**
     * Returns {@code "DNA Sample (mobName)"} when the item has a
     * {@link DnaSampleContents} component, or the plain translation key
     * {@code "item.mobfarmingsupplies.dna_sample.unknown"} otherwise.
     */
    @Override
    public Component getName(ItemStack stack) {
        DnaSampleContents contents = getContents(stack);
        if (contents != null) {
            return Component.translatable(
                    "item.mobfarmingsupplies.dna_sample",
                    Component.literal(contents.mobName()));
        }
        return Component.translatable("item.mobfarmingsupplies.dna_sample.unknown");
    }
}
