package net.bobofraggins.mobfarmingsupplies.fabric.client;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

/** A fluid's texture and tint, as Fabric's fluid render handlers give them. */
public final class FluidSprites {

    private FluidSprites() {}

    /** The still texture of {@code fluid}; the missing texture if it has none. */
    public static TextureAtlasSprite still(FluidStack fluid) {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(variant(fluid));
        return sprite != null ? sprite
                : Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
    }

    /**
     * The tint of {@code fluid} at {@code pos} (water takes its biome's colour), or as an item
     * when {@code level} is null. Its alpha is 0 for fluids that don't set one.
     */
    public static int tint(FluidStack fluid, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        return FluidVariantRendering.getColor(variant(fluid), level, pos);
    }

    private static FluidVariant variant(FluidStack fluid) {
        return FluidStackHooksFabric.toFabric(fluid.copyWithAmount(1));
    }
}
