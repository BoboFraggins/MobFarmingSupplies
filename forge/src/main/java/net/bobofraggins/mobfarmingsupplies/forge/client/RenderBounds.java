package net.bobofraggins.mobfarmingsupplies.forge.client;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * A block entity renderer that draws outside its block: the box it draws in, so it isn't culled too
 * soon (NeoForge has this on the renderer; Forge 1.20.1 asks the block entity, which
 * {@code BlockEntityRenderBoundsMixin} hands on to the renderer through {@link ForgeRenderBounds}).
 */
public interface RenderBounds<T extends BlockEntity> {
    AABB getRenderBoundingBox(T blockEntity);
}
