package net.bobofraggins.mobfarmingsupplies.forge.tank;

import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Experience Pylon renderer with Forge's culling box raised to cover the pylon. */
public class NeoExperiencePylonRenderer extends ExperiencePylonRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<ExperiencePylonBlockEntity> {

    public NeoExperiencePylonRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(ExperiencePylonBlockEntity pylon) {
        return renderBounds(pylon);
    }
}
