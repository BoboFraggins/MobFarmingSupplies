package net.bobofraggins.mobfarmingsupplies.neoforge.omnihopper;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperItemRenderer;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class OmniHopperClientEvents {

    private OmniHopperClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.OMNI_HOPPER_MENU.get(), OmniHopperScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "omnidirectional_hopper_renderer"),
                OmniHopperItemRenderer.Unbaked.MAP_CODEC);
    }
}
