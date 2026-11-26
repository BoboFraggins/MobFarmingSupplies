package net.bobofraggins.mobfarmingsupplies.forge.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Absorption/Essence Hopper area renderer with Forge's culling box widened to the pickup area. */
public class NeoAbsorptionHopperRenderer<T extends AbsorptionHopperBlockEntity> extends AbsorptionHopperBlockEntityRenderer<T>
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<T> {

    public NeoAbsorptionHopperRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(T be) {
        return renderBounds(be);
    }
}
