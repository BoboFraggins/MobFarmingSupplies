package net.bobofraggins.mobfarmingsupplies.neoforge.filterscribingterminal;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;

public final class FilterScribingTerminalClientEvents {

    private FilterScribingTerminalClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.FILTER_SCRIBING_TERMINAL_MENU.get(), FilterScribingTerminalScreen::new);
    }
}
