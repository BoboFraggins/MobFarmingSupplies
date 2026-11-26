package net.bobofraggins.mobfarmingsupplies.forge.client;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommonClient;
import net.bobofraggins.mobfarmingsupplies.forge.tank.TankItemRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialItemRenderers;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Gives every item drawn in code ({@link SpecialItemRenderers}) its renderer. */
public final class ItemRendererClientEvents {

    private ItemRendererClientEvents() {}

    /**
     * Forge 1.20.1 has no client-extensions event: an item's extensions come from its own
     * {@code initializeClient}, which this mod's common items can't override, so they're set on
     * the items here (the field Forge fills from {@code initializeClient}), during client setup.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
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
        try {
            java.lang.reflect.Field field = Item.class.getDeclaredField("renderProperties");
            field.setAccessible(true);
            for (Item item : SpecialItemRenderers.all().keySet()) field.set(item, extensions);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Couldn't give items their renderers", e);
        }
    }
}
