package net.bobofraggins.mobfarmingsupplies.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/** A fluid's texture and tint, as NeoForge's fluid client extensions give them. */
public final class FluidSprites {

    private FluidSprites() {}

    /** The still texture of {@code fluid}, from the block atlas. */
    public static TextureAtlasSprite still(FluidStack fluid) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
    }

    /** The ARGB tint of {@code fluid} (water's blue, for example); white when it has none. */
    public static int tint(FluidStack fluid) {
        return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
    }
}
