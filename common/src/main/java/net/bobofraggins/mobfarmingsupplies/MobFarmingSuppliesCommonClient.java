package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperScreen;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeMenu;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticScreen;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorScreen;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorItemRenderer;
import net.bobofraggins.mobfarmingsupplies.fan.FanScreen;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterItemRenderer;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterScreen;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterScreen;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadItemRenderer;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperItemRenderer;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperScreen;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketKeybinds;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialItemRenderers;
import net.bobofraggins.mobfarmingsupplies.tank.TankScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

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
        // MenuRegistry.registerScreenFactory adds a RegisterMenuScreensEvent
        // listener to architectury's mod bus, but by the time init() runs (during our
        // FMLClientSetupEvent) architectury's RegisterMenuScreensEvent has already
        // fired — so these calls are silent no-ops there. NeoForge registers these
        // screens directly via RegisterMenuScreensEvent in
        // FanClientEvents/MobHarvesterClientEvents/CloneOMaticClientEvents/
        // TankClientEvents/AbsorptionHopperClientEvents instead.
        MenuRegistry.registerScreenFactory(
                Registration.CLONE_O_MATIC_MENU.get(),
                CloneOMaticScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.FAN_MENU.get(),
                FanScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.MOB_HARVESTER_MENU.get(),
                MobHarvesterScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.TANK_MENU.get(),
                TankScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.ABSORPTION_HOPPER_MENU.get(),
                AbsorptionHopperScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.ENDER_INHIBITOR_MENU.get(),
                EnderInhibitorScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.PICNIC_BASKET_MENU.get(),
                PicnicBasketScreen::new);
        PicnicBasketKeybinds.init();
        net.bobofraggins.mobfarmingsupplies.magnet.MagnetKeybinds.init();
        MenuRegistry.registerScreenFactory(
                Registration.FILTER_SCRIBING_TERMINAL_MENU.get(),
                FilterScribingTerminalScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.LOGISTIC_SORTER_MENU.get(),
                LogisticSorterScreen::new);
        MenuRegistry.registerScreenFactory(
                Registration.OMNI_HOPPER_MENU.get(),
                OmniHopperScreen<OmniHopperMenu>::new);
        MenuRegistry.registerScreenFactory(
                Registration.BRIDGE_MENU.get(),
                OmniHopperScreen<EinsteinRosenBridgeMenu>::new);
        // The Superconducting Magnet's on/off look (its model's override predicate).
        ItemPropertiesRegistry.register(Registration.SUPERCONDUCTING_MAGNET.get(),
                ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "magnet_on"),
                (stack, level, entity, seed) -> stack.has(Registration.MAGNET_ON.get()) ? 1f : 0f);
    }

    /**
     * Registers the items drawn in code with {@link SpecialItemRenderers}. Called by each loader
     * before it hands those items their renderer; the Tanks are added by the loaders themselves.
     */
    public static void registerItemRenderers() {
        SpecialItemRenderers.register(Registration.OMNI_HOPPER_ITEM.get(),
                new OmniHopperItemRenderer(Registration.OMNI_HOPPER));
        SpecialItemRenderers.register(Registration.EINSTEIN_ROSEN_BRIDGE_ITEM.get(),
                new OmniHopperItemRenderer(Registration.EINSTEIN_ROSEN_BRIDGE));
        SpecialItemRenderers.register(Registration.LOGISTIC_SORTER_ITEM.get(), new LogisticSorterItemRenderer());
        SpecialItemRenderers.register(Registration.MOB_HEAD_ITEM.get(), new MobHeadItemRenderer());
        SpecialItemRenderers.register(Registration.ENDER_INHIBITOR_ITEM.get(), new InhibitorItemRenderer(
                Registration.ENDER_INHIBITOR, Items.ENDER_PEARL, InhibitorItemRenderer.Particles.PORTAL));
        SpecialItemRenderers.register(Registration.WITHER_INHIBITOR_ITEM.get(), new InhibitorItemRenderer(
                Registration.WITHER_INHIBITOR, Items.WITHER_SKELETON_SKULL, InhibitorItemRenderer.Particles.SMOKE));
    }
}
