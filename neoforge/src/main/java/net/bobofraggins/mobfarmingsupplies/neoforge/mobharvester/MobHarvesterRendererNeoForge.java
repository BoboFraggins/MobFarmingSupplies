package net.bobofraggins.mobfarmingsupplies.neoforge.mobharvester;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;

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
