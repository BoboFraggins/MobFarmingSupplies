package net.bobofraggins.mobfarmingsupplies.oresight;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Oresight, from drinking a Bucket of Oresight: blocks of one kind near the player are traced in
 * blue through walls. The effect only says that it's active; which block it shows is kept by
 * {@link Oresight} and drawn by {@link OresightClient}.
 */
public class OresightMobEffect extends MobEffect {

    public OresightMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3C8CFF);
    }
}
