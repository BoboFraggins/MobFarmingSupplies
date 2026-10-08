package net.bobofraggins.mobfarmingsupplies.neoforge.mobhead;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadItemRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class MobHeadClientEvents {

    private MobHeadClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.MOB_HEAD_BE_TYPE.get(), MobHeadBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "mob_head_renderer"),
                MobHeadItemRenderer.Unbaked.MAP_CODEC);
    }
}
