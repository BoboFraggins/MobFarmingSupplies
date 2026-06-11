package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankFluidGeometry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * Fabric port of the NeoForge {@code TankItemRenderer}.
 *
 * <p>Registered against vanilla's {@code SpecialModelRenderers.ID_MAPPER} via reflection
 * (see {@code MobFarmingSuppliesFabricClient}) under the id {@code mobfarmingsupplies:tank_renderer},
 * matching {@code items/tank.json}.
 */
@SuppressWarnings("UnstableApiUsage")
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
        FluidStack archFluid = data.storedFluid();

        var fluidModel = mc.getModelManager()
                .getFluidStateModelSet()
                .get(archFluid.getFluid().defaultFluidState());
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();

        int fluidTint = fluidModel.tintSource().color(archFluid.getFluid().defaultFluidState().createLegacyBlock());
        int fr = (fluidTint >> 16) & 0xFF;
        int fg = (fluidTint >>  8) & 0xFF;
        int fb = fluidTint & 0xFF;
        int fa = (fluidTint >> 24) & 0xFF;
        if (fa == 0) fa = 77;

        int luminance = FluidVariantAttributes.getLuminance(FluidStackHooksFabric.toFabric(archFluid.copyWithAmount(1)));
        int fluidLight = luminance > 0 ? 0xF000F0 : packedLight;
        float fillTop = TankFluidGeometry.FLOOR + fillFrac * TankFluidGeometry.H;
        float uL = sprite.getU0(), uR = sprite.getU1();
        float vT = sprite.getV0();
        float vB = Mth.lerp(fillFrac, sprite.getV0(), sprite.getV1());

        final int ffrF = fr, ffgF = fg, ffbF = fb, ffaF = fa, flF = fluidLight, overlayF = packedOverlay;
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(),
                (pose, vc) -> TankFluidGeometry.renderCubeFill(
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
