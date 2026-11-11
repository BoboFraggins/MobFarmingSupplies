package net.bobofraggins.mobfarmingsupplies.runestone;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;

/**
 * The runestones: charms, each crafted from Stone and four buckets of one {@link Essence}. What each
 * does while worn is in {@link Runestones}.
 */
public enum Runestone {
    /** No burning and no fire or lava damage. */
    FIRE_PROTECTION("fire_protection", Essence.FIRE),
    /** No Poison effect, so no poison damage. */
    POISON_PROTECTION("poison_protection", Essence.VENOM),
    /** No Wither effect and no wither or wither-skull damage. */
    WITHER_PROTECTION("wither_protection", Essence.WITHER),
    /** Air never runs out underwater. */
    WATER_BREATHING("water_breathing", Essence.WATER);

    private final String name;
    private final Essence essence;

    Runestone(String name, Essence essence) {
        this.name = name;
        this.essence = essence;
    }

    /** The item id, e.g. {@code runestone_fire_protection}. */
    public String itemId() { return "runestone_" + name; }

    /** The essence its recipe takes. */
    public Essence essence() { return essence; }
}
