package net.bobofraggins.mobfarmingsupplies.neoforge.oresight;

import net.bobofraggins.mobfarmingsupplies.oresight.OresightClient;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

/** NeoForge hooks for Oresight's x-ray outlines (see {@link OresightClient}). */
public final class OresightClientEvents {

    private OresightClientEvents() {}

    /** Mod bus: NeoForge compiles pipelines up front, so the x-ray one is registered here. */
    public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
        if (OresightClient.ownsPipeline()) event.registerPipeline(OresightClient.PIPELINE);
    }

    /** Game bus: submitted with the world's other custom geometry. */
    public static void onRenderLevel(SubmitCustomGeometryEvent event) {
        OresightClient.render(event.getPoseStack(), event.getSubmitNodeCollector());
    }
}
