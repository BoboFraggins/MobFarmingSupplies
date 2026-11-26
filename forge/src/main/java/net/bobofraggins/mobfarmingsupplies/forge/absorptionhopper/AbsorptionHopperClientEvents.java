package net.bobofraggins.mobfarmingsupplies.forge.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperScreen;
import net.bobofraggins.mobfarmingsupplies.essencehopper.EssenceHopperScreen;

public final class AbsorptionHopperClientEvents {

    private AbsorptionHopperClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                NeoAbsorptionHopperRenderer::new);
        event.registerBlockEntityRenderer(
                Registration.ESSENCE_HOPPER_BE_TYPE.get(),
                NeoAbsorptionHopperRenderer::new);
    }

}
