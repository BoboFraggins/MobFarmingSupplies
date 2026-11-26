package net.bobofraggins.mobfarmingsupplies.forge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightHookBlockEntity;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The String of Skull Lights renderer with Forge's culling box widened to the whole string. */
public class NeoSkullLightsRenderer extends SkullLightsRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<SkullLightHookBlockEntity> {

    public NeoSkullLightsRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(SkullLightHookBlockEntity hook) {
        return renderBounds(hook);
    }
}
