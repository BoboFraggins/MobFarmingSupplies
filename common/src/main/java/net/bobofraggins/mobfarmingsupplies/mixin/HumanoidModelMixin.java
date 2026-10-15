package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadWearer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides a humanoid's own head (and the hat layer, a child of it) while it wears a Mob Head, so
 * the mob's head stands in for it rather than sitting on top of it. Set every frame, since the
 * model is shared between entities.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {

    @Shadow @Final public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"))
    private void mfs$hideHeadUnderMobHead(HumanoidRenderState state, CallbackInfo ci) {
        head.visible = !((MobHeadWearer) state).mfs$wearsMobHead();
    }
}
