package net.bobofraggins.mobfarmingsupplies.neoforge.headpike;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeBlockEntity;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Head Pike renderer with NeoForge's culling box widened to both of the pike's blocks. */
public class NeoHeadPikeRenderer extends HeadPikeRenderer {

    public NeoHeadPikeRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(HeadPikeBlockEntity pike) {
        return renderBounds(pike);
    }
}
