package net.bobofraggins.mobfarmingsupplies.neoforge.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterItemRenderer;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class LogisticSorterClientEvents {

    private LogisticSorterClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.LOGISTIC_SORTER_MENU.get(), LogisticSorterScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "logistic_sorter_renderer"),
                LogisticSorterItemRenderer.Unbaked.MAP_CODEC);
    }
}
