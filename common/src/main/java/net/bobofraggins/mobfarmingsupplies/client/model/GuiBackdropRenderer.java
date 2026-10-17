package net.bobofraggins.mobfarmingsupplies.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * The soft light gray glow drawn behind dark items in inventory slots (the item definitions only
 * use it for the {@code gui} display context). A flat quad facing the viewer, placed behind the item
 * by its base model's {@code gui} transform.
 *
 * <p>Drawn as custom geometry ahead of the item's own layers ({@code order(-1)}), in a render type of
 * its own. The glass blocks' layers are translucent too, and translucent surfaces write depth even
 * where fully transparent, so a backdrop drawn after them would be hidden behind the whole cube.
 */
public class GuiBackdropRenderer implements SpecialModelRenderer<Void> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/gui_backdrop.png");
    private static final RenderType TYPE = RenderTypes.entityTranslucent(TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Nullable
    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int packedLight, int packedOverlay, boolean hasFoil, int tint) {
        collector.order(-1).submitCustomGeometry(poseStack, TYPE, (pose, vc) -> {
            vertex(vc, pose, 0, 0, 0, 1);
            vertex(vc, pose, 1, 0, 1, 1);
            vertex(vc, pose, 1, 1, 1, 0);
            vertex(vc, pose, 0, 1, 0, 0);
        });
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v) {
        vc.addVertex(pose, x, y, 0.5f).setColor(1f, 1f, 1f, 1f).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0f, 0f, 1f);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new GuiBackdropRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }
    }
}
