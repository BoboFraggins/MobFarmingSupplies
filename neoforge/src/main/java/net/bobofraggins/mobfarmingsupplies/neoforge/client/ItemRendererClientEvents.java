package net.bobofraggins.mobfarmingsupplies.neoforge.client;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommonClient;
import net.bobofraggins.mobfarmingsupplies.neoforge.tank.TankItemRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialItemRenderers;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Gives every item drawn in code ({@link SpecialItemRenderers}) its renderer. */
public final class ItemRendererClientEvents {

    private ItemRendererClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        SpecialItemRenderers.register(Registration.TANK_ITEM.get(), new TankItemRenderer(TankTier.BASIC));
        SpecialItemRenderers.register(Registration.GOLD_TANK_ITEM.get(), new TankItemRenderer(TankTier.GOLD));
        SpecialItemRenderers.register(Registration.DIAMOND_TANK_ITEM.get(), new TankItemRenderer(TankTier.DIAMOND));
        SpecialItemRenderers.register(Registration.EMERALD_TANK_ITEM.get(), new TankItemRenderer(TankTier.EMERALD));
        MobFarmingSuppliesCommonClient.registerItemRenderers();

        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return SpecialItemRenderers.itemRenderer();
            }
        };
        for (Item item : SpecialItemRenderers.all().keySet()) {
            event.registerItem(extensions, item);
        }
    }
}
