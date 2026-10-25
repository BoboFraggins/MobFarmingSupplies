package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * A giant lawn ornament as an item: the same posed figure, one block tall, standing in the item's
 * block space so the item model's display transforms place it. Which ornament is the item
 * definition's {@code ornament} field. A Giant Player Lawn Ornament item wears its owner's skin;
 * one without an owner (as in the creative tab) slowly crossfades between Steve and Alex.
 */
public class GiantOrnamentItemRenderer implements SpecialModelRenderer<GiantOrnamentItemRenderer.Argument> {

    /** Steve to Alex and back again, in milliseconds. */
    private static final float CROSSFADE_CYCLE_MS = 8000f;
    /** The crossfade moves in this many steps each way. */
    private static final int CROSSFADE_STEPS = 64;

    /**
     * What an item is drawn from: its owner, and for an ownerless player ornament how far it has
     * faded to Alex (0 to {@value #CROSSFADE_STEPS}). Inventory icons are cached and only redrawn
     * when this changes, so the fade step has to be part of it for the icon to move.
     */
    public record Argument(@Nullable ResolvableProfile owner, int alexStep) {}

    /** A figure is about two blocks tall; as an item it fits in one. */
    private static final float ITEM_SCALE = 0.5f;

    private final GiantOrnament ornament;
    @Nullable private GiantOrnamentRenderer.Models models;

    public GiantOrnamentItemRenderer(GiantOrnament ornament) {
        this.ornament = ornament;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    public Argument extractArgument(ItemStack stack) {
        ResolvableProfile owner = stack.get(DataComponents.PROFILE);
        if (ornament != GiantOrnament.PLAYER || owner != null) return new Argument(owner, 0);
        float alex = (1f - Mth.cos(Util.getMillis() / CROSSFADE_CYCLE_MS * Mth.TWO_PI)) / 2f;
        return new Argument(null, Math.round(alex * CROSSFADE_STEPS));
    }

    @Override
    public void submit(@Nullable Argument argument, PoseStack poseStack, SubmitNodeCollector collector,
                       int packedLight, int packedOverlay, boolean hasFoil, int tint) {
        ResolvableProfile owner = argument == null ? null : argument.owner();
        if (models == null) {
            models = GiantOrnamentRenderer.Models.bake(Minecraft.getInstance().getEntityModels()::bakeLayer);
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        // Faces south, the item's front.
        if (ornament == GiantOrnament.PLAYER && owner == null) {
            submitCrossfade(argument == null ? 0f : (float) argument.alexStep() / CROSSFADE_STEPS,
                    poseStack, collector, packedLight);
        } else {
            GiantOrnamentRenderer.submitFigure(models.look(ornament, owner, false),
                    GiantFigureModel.Pose.FORWARD, 0f, ITEM_SCALE, poseStack, collector, packedLight);
        }
        poseStack.popPose();
    }

    /**
     * Steve and Alex blended: whichever is mostly showing is drawn solid, and the other over it,
     * see-through by the share it has. Swapping which is solid halfway means each end of the cycle
     * is just one of them (Alex's slimmer arms don't keep a ghost of Steve's).
     */
    private void submitCrossfade(float alex, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        boolean alexSolid = alex > 0.5f;
        float overlay = alexSolid ? 1f - alex : alex;
        GiantOrnamentRenderer.submitFigure(models.ownerless(alexSolid, false),
                GiantFigureModel.Pose.FORWARD, 0f, ITEM_SCALE, poseStack, collector, light);
        int alpha = Math.round(overlay * 255f);
        if (alpha > 0) {
            GiantOrnamentRenderer.submitFigure(models.ownerless(!alexSolid, true),
                    GiantFigureModel.Pose.FORWARD, 0f, ITEM_SCALE, poseStack, collector, light,
                    alpha << 24 | 0xFFFFFF);
        }
    }

    public record Unbaked(GiantOrnament ornament) implements SpecialModelRenderer.Unbaked<Argument> {
        public static final MapCodec<Unbaked> MAP_CODEC = GiantOrnament.CODEC
                .optionalFieldOf("ornament", GiantOrnament.SKELETON)
                .xmap(Unbaked::new, Unbaked::ornament);

        @Override
        public SpecialModelRenderer<Argument> bake(SpecialModelRenderer.BakingContext context) {
            return new GiantOrnamentItemRenderer(ornament);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Argument>> type() {
            return MAP_CODEC;
        }
    }
}
