package net.bobofraggins.mobfarmingsupplies.client.model.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@OnlyIn(Dist.CLIENT)
public final class ExtraBlockModelsClientEvents {

    private ExtraBlockModelsClientEvents() {}

    @SubscribeEvent
    public static void onRegisterStandaloneModels(ModelEvent.RegisterStandalone event) {
        ExtraBlockModelsImpl.onRegisterStandalone(event);
    }
}
