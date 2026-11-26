package net.bobofraggins.mobfarmingsupplies.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Client side of {@code BlockEntityRenderBoundsMixin}: asks the block entity's renderer for its box. */
public final class ForgeRenderBounds {

    private ForgeRenderBounds() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static AABB of(BlockEntity be) {
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
        if (renderer instanceof RenderBounds bounds) return bounds.getRenderBoundingBox(be);
        return new AABB(be.getBlockPos());
    }
}
