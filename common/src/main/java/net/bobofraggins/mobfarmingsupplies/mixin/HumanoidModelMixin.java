package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides a humanoid's own head (and its hat layer) while it wears a Mob Head, so the mob's head
 * stands in for it rather than sitting on top of it. Set every frame, since the model is shared
 * between entities.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void mfs$hideHeadUnderMobHead(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        boolean wearsMobHead = entity.getItemBySlot(EquipmentSlot.HEAD).is(Registration.MOB_HEAD_ITEM.get());
        head.visible = !wearsMobHead;
        if (wearsMobHead) hat.visible = false;
    }
}
