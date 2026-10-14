package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadWearer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Notes in the render state whether the entity is wearing a Mob Head. */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;"
            + "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void mfs$noteMobHead(LivingEntity entity, LivingEntityRenderState state, float partialTicks,
                                 CallbackInfo ci) {
        ((MobHeadWearer) state).mfs$setWearsMobHead(
                entity.getItemBySlot(EquipmentSlot.HEAD).is(Registration.MOB_HEAD_ITEM.get()));
    }
}
