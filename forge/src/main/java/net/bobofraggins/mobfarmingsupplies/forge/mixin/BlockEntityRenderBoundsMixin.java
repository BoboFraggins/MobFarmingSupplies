package net.bobofraggins.mobfarmingsupplies.forge.mixin;

import net.bobofraggins.mobfarmingsupplies.forge.client.ForgeRenderBounds;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Gives the block entities whose renderers draw outside the block a render box from the renderer
 * ({@link net.bobofraggins.mobfarmingsupplies.forge.client.RenderBounds}): Forge 1.20.1 asks the block
 * entity ({@code IForgeBlockEntity.getRenderBoundingBox}), NeoForge 1.21 the renderer. The method
 * only runs on the client, and the client classes are behind {@link ForgeRenderBounds}.
 */
@Mixin({
        net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightHookBlockEntity.class,
        net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradeStationBlockEntity.class
})
public abstract class BlockEntityRenderBoundsMixin {

    public AABB getRenderBoundingBox() {
        return ForgeRenderBounds.of((BlockEntity) (Object) this);
    }
}
