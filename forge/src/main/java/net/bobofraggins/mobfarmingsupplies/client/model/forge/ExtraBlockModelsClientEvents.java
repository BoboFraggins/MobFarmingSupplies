package net.bobofraggins.mobfarmingsupplies.client.model.forge;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ModelEvent;

public final class ExtraBlockModelsClientEvents {

    private ExtraBlockModelsClientEvents() {}

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ExtraBlockModelsImpl.onRegisterAdditional(event);
    }
}
