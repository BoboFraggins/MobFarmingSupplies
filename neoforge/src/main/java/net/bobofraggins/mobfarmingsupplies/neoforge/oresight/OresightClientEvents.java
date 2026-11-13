package net.bobofraggins.mobfarmingsupplies.neoforge.oresight;

import net.bobofraggins.mobfarmingsupplies.oresight.OresightClient;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** NeoForge hooks for Oresight's x-ray outlines (see {@link OresightClient}). */
public final class OresightClientEvents {

    private OresightClientEvents() {}

    /** Mod bus: NeoForge compiles pipelines up front, so the x-ray one is registered here. */
    public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(OresightClient.PIPELINE);
    }

    /** Game bus: drawn after everything else in the world. */
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        OresightClient.render(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource());
    }
}
