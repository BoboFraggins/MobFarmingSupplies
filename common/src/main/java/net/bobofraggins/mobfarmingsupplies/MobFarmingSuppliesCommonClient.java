package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperScreen;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticScreen;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorScreen;
import net.bobofraggins.mobfarmingsupplies.fan.FanScreen;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankScreen;

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
                Registration.MOB_HARVESTER_MENU.get(),
                MobHarvesterScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.TANK_MENU.get(),
                TankScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.ABSORPTION_HOPPER_MENU.get(),
                AbsorptionHopperScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                Registration.ENDER_INHIBITOR_MENU.get(),
                EnderInhibitorScreen::new);
    }
}
