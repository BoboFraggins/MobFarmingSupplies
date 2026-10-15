package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.neoforge.client.FluidSprites;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.Geometry;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class TankItemRenderer implements SpecialModelRenderer<TankContents> {

    /** Which tank this renders: decides the case model and the capacity the fill level is relative to. */
    private final TankTier tier;

    public TankItemRenderer(TankTier tier) {
        this.tier = tier;
    }

    @Override
    @Nullable
    public TankContents extractArgument(ItemStack stack) {
        return stack.get(Registration.TANK_CONTENTS.get());
    }

    @Override
    public void submit(
            @Nullable TankContents data,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            boolean hasFoil) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                tier.block().defaultBlockState(), poseStack, buffers, packedLight, packedOverlay);

        if (data == null || data.isEmpty()) return;

        float fillFrac = Math.min(1f, Math.max(0.01f, (float) data.amount() / tier.capacity()));
        dev.architectury.fluid.FluidStack archFluid = data.storedFluid();
        FluidStack fluid = FluidStackHooksForge.toForge(archFluid.copyWithAmount(1));

        TextureAtlasSprite sprite = FluidSprites.still(fluid);
        int fluidTint = FluidSprites.tint(fluid);
        int fr = (fluidTint >> 16) & 0xFF;
        int fg = (fluidTint >>  8) & 0xFF;
        int fb = fluidTint & 0xFF;
        int fa = (fluidTint >> 24) & 0xFF;
        if (fa == 0) fa = 77;

        int fluidLight = fluid.getFluidType().getLightLevel() > 0 ? 0xF000F0 : packedLight;
        float fillTop = TankRenderer.FLOOR + fillFrac * TankRenderer.H;
        float uL = sprite.getU0(), uR = sprite.getU1();
        float vT = sprite.getV0();
        float vB = Mth.lerp(fillFrac, sprite.getV0(), sprite.getV1());
        float vMax = sprite.getV1();

        final int ffrF = fr, ffgF = fg, ffbF = fb, ffaF = fa, flF = fluidLight, overlayF = packedOverlay;
        Geometry.custom(poseStack, buffers, Sheets.translucentItemSheet(),
                (pose, vc) -> TankRenderer.renderCubeFill(
                        vc, pose, ffrF, ffgF, ffbF, ffaF, flF, overlayF, uL, vT, uR, vB, vMax, fillTop));
    }
}
