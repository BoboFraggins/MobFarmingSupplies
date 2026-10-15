package net.bobofraggins.mobfarmingsupplies.mobhead;

/**
 * Added to every living entity's render state: whether it is wearing a Mob Head, so the model
 * can hide its own head underneath (see {@code HumanoidModelMixin}).
 */
public interface MobHeadWearer {

    boolean mfs$wearsMobHead();

    void mfs$setWearsMobHead(boolean wears);
}
