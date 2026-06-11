package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class TankItemRenderer implements SpecialModelRenderer<TankContents> {

    /** Cached once on first render — block model parts never change after bake. */
    private List<BlockStateModelPart> cachedBlkParts = null;

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    @Nullable
    public TankContents extractArgument(ItemStack stack) {
        return stack.get(Registration.TANK_CONTENTS.get());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void submit(
            @Nullable TankContents data,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay,
            boolean hasFoil,
            int tint) {
        Minecraft mc = Minecraft.getInstance();

        if (cachedBlkParts == null) {
            BlockStateModel blkModel = mc.getModelManager()
                    .getBlockStateModelSet()
                    .get(Registration.TANK.get().defaultBlockState());
            List<BlockStateModelPart> parts = new ArrayList<>();
            blkModel.collectParts(RandomSource.create(), parts);
            cachedBlkParts = parts;
        }
        List<BlockStateModelPart> blkParts = cachedBlkParts;
        collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, vc) -> {
            QuadInstance qi = new QuadInstance();
            qi.setColor(0xFFFFFFFF);
            qi.setLightCoords(packedLight);
            qi.setOverlayCoords(packedOverlay);
            for (BlockStateModelPart part : blkParts) {
                for (Direction dir : Direction.values()) {
                    for (var quad : part.getQuads(dir)) {
                        vc.putBakedQuad(pose, quad, qi);
                    }
                }
                for (var quad : part.getQuads(null)) {
                    vc.putBakedQuad(pose, quad, qi);
                }
            }
        });

        if (data == null || data.isEmpty()) return;

        float fillFrac = Math.max(0.01f, (float) data.amount() / TankBlockEntity.CAPACITY);
        dev.architectury.fluid.FluidStack archFluid = data.storedFluid();
        FluidStack fluid = FluidStackHooksForge.toForge(archFluid.copyWithAmount(1));

        var fluidModel = mc.getModelManager()
                .getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState());
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();

        int fluidTint = fluidModel.fluidTintSource() != null
                ? fluidModel.fluidTintSource().colorAsStack(fluid)
                : 0xFFFFFFFF;
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

        final int ffrF = fr, ffgF = fg, ffbF = fb, ffaF = fa, flF = fluidLight, overlayF = packedOverlay;
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(),
                (pose, vc) -> TankRenderer.renderCubeFill(
                        vc, pose.pose(), ffrF, ffgF, ffbF, ffaF, flF, overlayF, uL, vT, uR, vB, fillTop));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<TankContents> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        @Nullable
        public SpecialModelRenderer<TankContents> bake(SpecialModelRenderer.BakingContext context) {
            return new TankItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<TankContents>> type() {
            return MAP_CODEC;
        }
    }
}
