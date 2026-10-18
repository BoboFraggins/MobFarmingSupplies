package net.bobofraggins.mobfarmingsupplies.neoforge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantSkeletonOrnamentItemRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class LawnOrnamentClientEvents {

    private LawnOrnamentClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.GIANT_SKELETON_ORNAMENT_BE_TYPE.get(),
                NeoGiantSkeletonOrnamentRenderer::new);
        event.registerBlockEntityRenderer(Registration.SKULL_LIGHT_HOOK_BE_TYPE.get(), NeoSkullLightsRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "giant_skeleton_lawn_ornament"),
                GiantSkeletonOrnamentItemRenderer.Unbaked.MAP_CODEC);
    }
}
