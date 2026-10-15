package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadWearer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements MobHeadWearer {

    @Unique
    private boolean mfs$wearsMobHead;

    @Override
    public boolean mfs$wearsMobHead() {
        return mfs$wearsMobHead;
    }

    @Override
    public void mfs$setWearsMobHead(boolean wears) {
        mfs$wearsMobHead = wears;
    }
}
