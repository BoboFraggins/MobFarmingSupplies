package net.bobofraggins.mobfarmingsupplies.shared.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A block entity renderer split the way later versions split them: copy what's needed out of the
 * block entity into a render state ({@link #extractRenderState}), then draw from that state alone
 * ({@link #submit}). One state is reused for every block entity this renderer draws; rendering
 * runs on the render thread only.
 */
public abstract class StateBlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState>
        implements BlockEntityRenderer<T> {

    @Nullable private S state;

    public abstract S createRenderState();

    /** Fills {@code state} from {@code be}; the base fields are already set. */
    public abstract void extractRenderState(T be, S state, float partialTick);

    public abstract void submit(S state, PoseStack poseStack, MultiBufferSource buffers);

    @Override
    public final void render(T be, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                             int light, int overlay) {
        if (state == null) state = createRenderState();
        BlockEntityRenderState.extractBase(be, state, light, overlay);
        extractRenderState(be, state, partialTick);
        submit(state, poseStack, buffers);
    }
}
