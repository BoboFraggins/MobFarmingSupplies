package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.minecraft.world.item.Item;

/**
 * A bottle of an essence: {@link #MB} of it, a quarter of a bucket. A glass bottle fills from a
 * tank of essence, in the Tank's window or by using it on any block with a fluid inventory
 * ({@link EssenceBottleEvents}); this pours back the same ways. Essence of Air and Essence of
 * Sight bottles are brewed into potions ({@code MFSBrewing}).
 */
public class EssenceBottleItem extends Item {

    /** How much essence a bottle holds, as a water bottle does. */
    public static final int MB = 250;

    private final Essence essence;

    public EssenceBottleItem(Essence essence, Properties properties) {
        super(properties);
        this.essence = essence;
    }

    public Essence essence() { return essence; }
}
