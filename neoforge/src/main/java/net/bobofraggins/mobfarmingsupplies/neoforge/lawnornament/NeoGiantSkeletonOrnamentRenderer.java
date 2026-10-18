package net.bobofraggins.mobfarmingsupplies.neoforge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantSkeletonOrnamentBlockEntity;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantSkeletonOrnamentRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The ornament's renderer with NeoForge's culling box widened to the whole skeleton. */
public class NeoGiantSkeletonOrnamentRenderer extends GiantSkeletonOrnamentRenderer {

    public NeoGiantSkeletonOrnamentRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(GiantSkeletonOrnamentBlockEntity ornament) {
        return renderBounds(ornament);
    }
}
