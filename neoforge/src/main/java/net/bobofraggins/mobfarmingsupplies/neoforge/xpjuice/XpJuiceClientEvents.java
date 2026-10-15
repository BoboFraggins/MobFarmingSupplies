package net.bobofraggins.mobfarmingsupplies.neoforge.xpjuice;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.neoforge.register.NeoForgeOnlyRegistration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class XpJuiceClientEvents {

    private static final ResourceLocation STILL =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_still");
    private static final ResourceLocation FLOW =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_flow");

    private XpJuiceClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF39FF14;
            }
        }, NeoForgeOnlyRegistration.XP_JUICE_TYPE.get());
    }
}
