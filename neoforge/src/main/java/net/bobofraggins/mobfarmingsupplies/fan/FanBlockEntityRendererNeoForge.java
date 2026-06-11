package net.bobofraggins.mobfarmingsupplies.fan;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;

/**
 * NeoForge-only widening of the render bounding box so the push-AABB wireframe
 * (up to 5 blocks deep) is not culled prematurely. {@code IBlockEntityRendererExtension}
 * is a NeoForge-only API, so this subclass cannot live in {@code common}.
 */
public final class FanBlockEntityRendererNeoForge extends FanBlockEntityRenderer
        implements IBlockEntityRendererExtension<FanBlockEntity> {

    public FanBlockEntityRendererNeoForge(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(FanBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(6);
    }
}
