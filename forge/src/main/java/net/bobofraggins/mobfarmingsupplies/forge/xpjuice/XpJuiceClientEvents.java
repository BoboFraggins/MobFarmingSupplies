package net.bobofraggins.mobfarmingsupplies.forge.xpjuice;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.forge.register.ForgeOnlyRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public final class XpJuiceClientEvents {

    private static final ResourceLocation STILL =
            new ResourceLocation(MobFarmingSuppliesCommon.MODID, "block/xp_juice_still");
    private static final ResourceLocation FLOW =
            new ResourceLocation(MobFarmingSuppliesCommon.MODID, "block/xp_juice_flow");

    private XpJuiceClientEvents() {}

    /** XP Juice's textures (Forge 1.20.1 asks the fluid type for these in {@code initializeClient}). */
    public static IClientFluidTypeExtensions xpJuice() {
        return textures(STILL, FLOW);
    }

    /** An essence's textures. */
    public static IClientFluidTypeExtensions essence(Essence essence) {
        return textures(new ResourceLocation(MobFarmingSuppliesCommon.MODID, "block/" + essence.fluidId() + "_still"),
                new ResourceLocation(MobFarmingSuppliesCommon.MODID, "block/" + essence.fluidId() + "_flow"));
    }

    private static IClientFluidTypeExtensions textures(ResourceLocation still, ResourceLocation flow) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flow;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF; // no tint: the textures are coloured themselves
            }
        };
    }
}
