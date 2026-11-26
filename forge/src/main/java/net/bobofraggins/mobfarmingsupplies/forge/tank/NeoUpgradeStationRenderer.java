package net.bobofraggins.mobfarmingsupplies.forge.tank;

import net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradeStationBlockEntity;
import net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradeStationRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

/** The Upgrade Station renderer with Forge's culling box widened to the open lid. */
public class NeoUpgradeStationRenderer extends UpgradeStationRenderer
        implements net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds<UpgradeStationBlockEntity> {

    public NeoUpgradeStationRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(UpgradeStationBlockEntity station) {
        return renderBounds(station);
    }
}
