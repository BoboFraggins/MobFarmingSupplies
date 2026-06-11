package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;

/**
 * NeoForge-only widening of the render bounding box so the swinging arms and
 * head are not culled prematurely. {@code IBlockEntityRendererExtension} is a
 * NeoForge-only API, so this subclass cannot live in {@code common}.
 */
public final class MobHarvesterRendererNeoForge extends MobHarvesterRenderer
        implements IBlockEntityRendererExtension<MobHarvesterBlockEntity> {

    public MobHarvesterRendererNeoForge(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(MobHarvesterBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(2.5);
    }
}
