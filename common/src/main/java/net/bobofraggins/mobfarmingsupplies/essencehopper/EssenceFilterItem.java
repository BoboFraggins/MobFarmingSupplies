package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.minecraft.world.item.Item;

/** An Essence Filter: in an Essence Hopper's slot, chooses which essence the hopper extracts. */
public class EssenceFilterItem extends Item {

    private final Essence essence;

    public EssenceFilterItem(Essence essence, Properties properties) {
        super(properties);
        this.essence = essence;
    }

    public Essence essence() {
        return essence;
    }
}
