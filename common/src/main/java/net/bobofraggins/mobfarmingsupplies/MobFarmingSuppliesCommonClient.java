package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperScreen;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeMenu;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticScreen;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorScreen;
import net.bobofraggins.mobfarmingsupplies.fan.FanScreen;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeScreen;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterScreen;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterScreen;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperScreen;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketKeybinds;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankScreen;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonScreen;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorScreen;

/**
 * Client-side initialisation shared by both loaders.
 *
 * <p>Only registers renderer/screen pairs where both sides are in common/.
 * NeoForge-specific renderers (Tank, AbsorptionHopper, CloneOMatic BE, Fan BE,
 * MobHarvester BE) and standalone-model registrations stay in the NeoForge
 * client-event classes until those features are migrated to Architectury.
 */
public final class MobFarmingSuppliesCommonClient {

    private MobFarmingSuppliesCommonClient() {}

    public static void init() {
        // Development only: renders block icons for the docs when MFS_RENDER_ICONS is set.
        net.bobofraggins.mobfarmingsupplies.client.devtools.BlockIconRenderer.register();
        // Development only: runs a command script as the player when MFS_DEV_COMMANDS is set.
        net.bobofraggins.mobfarmingsupplies.client.devtools.DevCommands.register();
        // Menu → screen bindings (menu and screen both in common). On Fabric this is
        // the only registration path and works correctly. On NeoForge,
        // MenuScreenRegistry.registerScreenFactory adds a RegisterMenuScreensEvent
        // listener to architectury's mod bus, but by the time init() runs (during our
        // FMLClientSetupEvent) architectury's RegisterMenuScreensEvent has already
        // fired — so these calls are silent no-ops there. NeoForge registers these
        // screens directly via RegisterMenuScreensEvent in
        // FanClientEvents/MobHarvesterClientEvents/CloneOMaticClientEvents/
        // TankClientEvents/AbsorptionHopperClientEvents instead.
        MenuScreenRegistry.registerScreenFactory(
                Registration.CLONE_O_MATIC_MENU.get(),
                CloneOMaticScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.FAN_MENU.get(),
                FanScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.HEAD_PIKE_MENU.get(),
                HeadPikeScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.MOB_HARVESTER_MENU.get(),
                MobHarvesterScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.TANK_MENU.get(),
                TankScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.EXPERIENCE_PYLON_MENU.get(),
                ExperiencePylonScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.INCUBATOR_MENU.get(),
                IncubatorScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.ABSORPTION_HOPPER_MENU.get(),
                AbsorptionHopperScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.ENDER_INHIBITOR_MENU.get(),
                EnderInhibitorScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.PICNIC_BASKET_MENU.get(),
                PicnicBasketScreen::new);
        PicnicBasketKeybinds.init();
        net.bobofraggins.mobfarmingsupplies.magnet.MagnetKeybinds.init();
        MenuScreenRegistry.registerScreenFactory(
                Registration.FILTER_SCRIBING_TERMINAL_MENU.get(),
                FilterScribingTerminalScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.LOGISTIC_SORTER_MENU.get(),
                LogisticSorterScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.OMNI_HOPPER_MENU.get(),
                OmniHopperScreen<OmniHopperMenu>::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.BRIDGE_MENU.get(),
                OmniHopperScreen<EinsteinRosenBridgeMenu>::new);
    }
}
