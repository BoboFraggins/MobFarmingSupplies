package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.brain.BrainSoaking;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives item entities a soak timer, for Zombie Brains cleaning in XP Juice ({@link BrainSoaking}). */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySoakMixin {

    @Unique
    private int mobfarmingsupplies$soaked;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mobfarmingsupplies$soak(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.isRemoved()) return;
        mobfarmingsupplies$soaked = BrainSoaking.tick(self, mobfarmingsupplies$soaked);
    }
}
