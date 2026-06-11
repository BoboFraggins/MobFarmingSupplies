package net.bobofraggins.mobfarmingsupplies.dna;

import net.minecraft.world.item.Item;

/**
 * DNA Collector — a single-use tool that samples a mob's identity.
 *
 * <p>Right-clicking a non-player {@link net.minecraft.world.entity.LivingEntity} consumes
 * one collector from the stack and places a {@link DnaSampleItem} (containing the mob's
 * full save NBT and a display name) directly into the player's inventory.
 * The actual interaction logic lives in {@link DnaCollectorEvents}, hooked via
 * {@link dev.architectury.event.events.common.InteractionEvent#INTERACT_ENTITY} so it
 * runs before ridable/tameable mobs (horses, etc.) consume the right-click themselves.
 *
 * <p>The display name stored in {@link DnaSampleContents#mobName()} is:
 * <ul>
 *   <li>the mob's custom name string, if one is set ({@link net.minecraft.world.entity.Entity#hasCustomName()})</li>
 *   <li>otherwise, the entity type's translation string (e.g. {@code "Zombie"})</li>
 * </ul>
 *
 * <p>Stacks to {@value #STACK_SIZE}.
 */
public class DnaCollectorItem extends Item {

    public static final int STACK_SIZE = 16;

    public DnaCollectorItem(Properties props) {
        super(props);
    }
}
