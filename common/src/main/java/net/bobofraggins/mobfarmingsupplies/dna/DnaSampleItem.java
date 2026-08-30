package net.bobofraggins.mobfarmingsupplies.dna;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * DNA Sample — holds the complete save data of a specific mob.
 *
 * <p>Created exclusively by {@link DnaCollectorItem} when right-clicking a
 * non-player {@link net.minecraft.world.entity.LivingEntity}.  The sample
 * carries a {@link DnaSampleContents} data component that stores the entity
 * NBT (including type) and a human-readable mob name used as the item's display
 * name.
 *
 * <p>DNA Samples do not stack (maxStackSize = 1) because each one may represent
 * a distinct mob identity (variant, equipment, name, etc.).
 *
 * <p>When the Clone-O-Matic's DNA-sample design is finalised, instances of this
 * item placed in its nine DNA slots will define which mob is spawned.
 */
public class DnaSampleItem extends Item implements IDnaSampleItem {

    public DnaSampleItem(Properties props) {
        super(props);
    }

    /**
     * Returns the {@link DnaSampleContents} stored in this stack, or {@code null} if absent.
     * Prefer this helper over accessing the data component type directly to avoid
     * circular import issues.
     */
    @Nullable
    public static DnaSampleContents getContents(ItemStack stack) {
        return stack.get(Registration.DNA_SAMPLE_CONTENTS.get());
    }

    @Override
    @Nullable
    public Entity createSpawnEntity(ItemStack stack, ServerLevel level, RandomSource random) {
        DnaSampleContents contents = getContents(stack);
        if (contents == null) return null;
        Entity entity = EntityType.loadEntityRecursive(
                contents.entityNbt(), level, new EntitySpawnRequest(EntitySpawnReason.SPAWNER, false), EntityProcessor.NOP);
        if (entity == null) return null;

        // The saved NBT carries the original captured mob's UUID. Every clone made
        // from this sample would otherwise share that UUID, and the level refuses to
        // add an entity whose UUID is already in use - so only one clone could ever
        // exist at a time. Give each spawned clone its own identity.
        entity.setUUID(Mth.createInsecureUUID(random));
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
