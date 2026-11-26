package net.bobofraggins.mobfarmingsupplies.forge.fan;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntity;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;

/**
 * Forge-only widening of the render bounding box so the push-AABB wireframe
 * (up to 5 blocks deep) is not culled prematurely. the render box
 * comes from {@link net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds}, so this subclass cannot live in {@code common}.
 */
public final class FanBlockEntityRendererNeoForge extends FanBlockEntityRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<FanBlockEntity> {

    public FanBlockEntityRendererNeoForge(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(FanBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(6);
    }
}
