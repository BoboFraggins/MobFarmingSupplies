package net.bobofraggins.mobfarmingsupplies.neoforge.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class LogisticSorterClientEvents {

    private LogisticSorterClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.LOGISTIC_SORTER_MENU.get(), LogisticSorterScreen::new);
    }
}
