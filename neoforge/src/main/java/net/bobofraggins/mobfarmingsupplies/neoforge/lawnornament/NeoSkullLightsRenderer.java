package net.bobofraggins.mobfarmingsupplies.neoforge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightHookBlockEntity;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The String of Skull Lights renderer with NeoForge's culling box widened to the whole string. */
public class NeoSkullLightsRenderer extends SkullLightsRenderer {

    public NeoSkullLightsRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(SkullLightHookBlockEntity hook) {
        return renderBounds(hook);
    }
}
