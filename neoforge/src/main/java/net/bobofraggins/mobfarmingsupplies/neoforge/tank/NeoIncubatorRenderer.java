package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Incubator renderer with NeoForge's culling box widened to the open lid. */
public class NeoIncubatorRenderer extends IncubatorRenderer {

    public NeoIncubatorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(IncubatorBlockEntity incubator) {
        return renderBounds(incubator);
    }
}
