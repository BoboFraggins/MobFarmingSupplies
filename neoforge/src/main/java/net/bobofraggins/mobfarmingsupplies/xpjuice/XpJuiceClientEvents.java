package net.bobofraggins.mobfarmingsupplies.xpjuice;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.NeoForgeOnlyRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

public final class XpJuiceClientEvents {

    private static final Identifier STILL =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_still");
    private static final Identifier FLOW =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_flow");

    private XpJuiceClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {}, NeoForgeOnlyRegistration.XP_JUICE_TYPE.get());
    }

    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        var unbaked = new FluidModel.Unbaked(
                new Material(STILL),
                new Material(FLOW),
                null,
                FluidTintSources.constant(0xFF39FF14));
        event.register(unbaked, Registration.XP_JUICE_SOURCE.get(), Registration.XP_JUICE_FLOWING.get());
    }
}
