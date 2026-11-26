package net.bobofraggins.mobfarmingsupplies.forge.oresight;

import net.bobofraggins.mobfarmingsupplies.oresight.OresightClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/** NeoForge hook for Oresight's x-ray outlines (see {@link OresightClient}). */
public final class OresightClientEvents {

    private OresightClientEvents() {}

    /** Game bus: drawn after the world's translucent blocks. */
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            OresightClient.render(event.getPoseStack());
        }
    }
}
