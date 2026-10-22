package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Experience Pylon renderer with NeoForge's culling box raised to cover the pylon. */
public class NeoExperiencePylonRenderer extends ExperiencePylonRenderer {

    public NeoExperiencePylonRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(ExperiencePylonBlockEntity pylon) {
        return renderBounds(pylon);
    }
}
