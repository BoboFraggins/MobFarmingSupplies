package net.bobofraggins.mobfarmingsupplies.forge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentBlockEntity;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The giant lawn ornament renderer with Forge's culling box widened to the whole figure. */
public class NeoGiantOrnamentRenderer extends GiantOrnamentRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<GiantOrnamentBlockEntity> {

    public NeoGiantOrnamentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(GiantOrnamentBlockEntity ornament) {
        return renderBounds(ornament);
    }
}
