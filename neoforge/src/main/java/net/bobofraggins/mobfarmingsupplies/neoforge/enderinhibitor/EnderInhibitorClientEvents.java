package net.bobofraggins.mobfarmingsupplies.neoforge.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.AnimatedItemModel;
import net.bobofraggins.mobfarmingsupplies.client.model.GuiBackdropRenderer;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorScreen;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorItemRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class EnderInhibitorClientEvents {

    private EnderInhibitorClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.ENDER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                Registration.WITHER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::wither);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "inhibitor_item"),
                InhibitorItemRenderer.Unbaked.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "gui_backdrop"),
                GuiBackdropRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "animated"),
                AnimatedItemModel.Unbaked.MAP_CODEC);
    }

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.ENDER_INHIBITOR_MENU.get(), EnderInhibitorScreen::new);
    }
}
