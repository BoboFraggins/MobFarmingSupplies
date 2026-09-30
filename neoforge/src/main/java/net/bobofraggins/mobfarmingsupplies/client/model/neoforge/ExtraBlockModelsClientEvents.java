package net.bobofraggins.mobfarmingsupplies.client.model.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class ExtraBlockModelsClientEvents {

    private ExtraBlockModelsClientEvents() {}

    @SubscribeEvent
    public static void onRegisterStandaloneModels(ModelEvent.RegisterStandalone event) {
        ExtraBlockModelsImpl.onRegisterStandalone(event);
    }
}
