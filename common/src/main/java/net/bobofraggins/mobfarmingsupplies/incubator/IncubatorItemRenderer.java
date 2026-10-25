package net.bobofraggins.mobfarmingsupplies.incubator;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Incubator as an item: its base and closed lid, drawn translucent so the lid is
 * see-through. The block draws its base in the cutout layer (so the lid, drawn later by
 * {@link IncubatorRenderer}, doesn't hide it), which an item would follow and lose the glass.
 */
public class IncubatorItemRenderer implements SpecialModelRenderer<Void> {

    public static final ResourceLocation BASE_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/incubator_base");

    @Nullable
    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, ItemDisplayContext context, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay, boolean hasFoil) {
        for (ResourceLocation id : new ResourceLocation[] {BASE_MODEL_ID, IncubatorRenderer.LID_MODEL_ID}) {
            BakedModel model = ExtraBlockModels.get(id);
            if (model != null) {
                ExtraBlockModels.render(poseStack, buffers, model, Sheets.translucentItemSheet(), packedLight, packedOverlay);
            }
        }
    }
}
