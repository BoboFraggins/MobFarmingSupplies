package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.bobofraggins.mobfarmingsupplies.tank.TankScreen;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonScreen;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorScreen;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilScreen;

public final class TankClientEvents {

    private TankClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.TANK_BE_TYPE.get(), TankRenderer::new);
        event.registerBlockEntityRenderer(Registration.EXPERIENCE_PYLON_BE_TYPE.get(), NeoExperiencePylonRenderer::new);
        event.registerBlockEntityRenderer(Registration.INCUBATOR_BE_TYPE.get(), NeoIncubatorRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.TANK_MENU.get(), TankScreen::new);
        event.register(Registration.EXPERIENCE_PYLON_MENU.get(), ExperiencePylonScreen::new);
        event.register(Registration.INCUBATOR_MENU.get(), IncubatorScreen::new);
        event.register(Registration.XP_ANVIL_MENU.get(), XpAnvilScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "tank_renderer"),
                TankItemRenderer.Unbaked.MAP_CODEC);
    }
}
