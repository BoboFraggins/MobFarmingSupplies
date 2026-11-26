package net.bobofraggins.mobfarmingsupplies.forge.tank;

import net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlockEntity;
import net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Color Workstation renderer with Forge's culling box raised to cover the upper half. */
public class NeoColorWorkstationRenderer extends ColorWorkstationRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<ColorWorkstationBlockEntity> {

    public NeoColorWorkstationRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(ColorWorkstationBlockEntity be) {
        return renderBounds(be);
    }
}
