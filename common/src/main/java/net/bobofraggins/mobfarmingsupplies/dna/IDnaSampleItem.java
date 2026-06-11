package net.bobofraggins.mobfarmingsupplies.dna;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Marker + strategy interface for all DNA Sample item types accepted by the Clone-O-Matic.
 *
 * <p>Implementors:
 * <ul>
 *   <li>{@link DnaSampleItem} — holds full entity NBT for a specific mob</li>
 *   <li>{@link DnaSamplePackItem} — represents a configurable pool of entity types</li>
 * </ul>
 */
public interface IDnaSampleItem {

    /**
     * Creates an entity to spawn from this DNA sample stack.
     *
     * @param stack  the item stack in the Clone-O-Matic slot
     * @param level  the server level to spawn into
     * @param random a random source (typically {@code level.getRandom()})
     * @return a newly constructed {@link Entity} not yet added to the world,
     *         or {@code null} if the sample is empty / has invalid data
     */
    @Nullable
    Entity createSpawnEntity(ItemStack stack, ServerLevel level, RandomSource random);
}
