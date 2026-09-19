package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only mod-bus events for the Magic Hat's render layer.
 *
 * <p>Deliberately Curios-free — this class is registered wholesale via
 * {@code modEventBus.register(MagicHatClientEvents.class)}, so it's always loaded regardless
 * of whether Curios is installed. Curios-specific registration lives in
 * {@link MagicHatCurioSetup}, which is only ever loaded after confirming Curios is present —
 * see that class's javadoc for why the split matters.
 */
@OnlyIn(Dist.CLIENT)
public final class MagicHatClientEvents {

    private MagicHatClientEvents() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new MagicHatHelmetLayer<>(renderer));
            }
        }

        addHatLayerTo(event, EntityTypes.ARMOR_STAND, ArmorStandRenderState.class);
        addHatLayerTo(event, EntityTypes.ZOMBIE, ZombieRenderState.class);
    }

    @SuppressWarnings("unchecked")
    private static <
                    E extends net.minecraft.world.entity.LivingEntity,
                    S extends net.minecraft.client.renderer.entity.state.HumanoidRenderState>
            void addHatLayerTo(EntityRenderersEvent.AddLayers event, EntityType<E> type, Class<S> stateClass) {
        LivingEntityRenderer<E, S, HumanoidModel<S>> renderer =
                (LivingEntityRenderer<E, S, HumanoidModel<S>>) event.getRenderer(type);
        if (renderer != null) {
            renderer.addLayer(new MagicHatHelmetLayer<>(renderer));
        }
    }
}
