package net.bobofraggins.mobfarmingsupplies.disenchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

/**
 * The XP Powered Disenchanter's floating book: vanilla's enchanting table renderer, drawing the
 * vanilla book animation the disenchanter keeps ({@link DisenchanterBlockEntity#book}), so it
 * floats, opens and turns towards nearby players just as an enchanting table's does.
 */
public class DisenchanterRenderer implements BlockEntityRenderer<DisenchanterBlockEntity> {

    private final EnchantTableRenderer vanilla;

    public DisenchanterRenderer(BlockEntityRendererProvider.Context ctx) {
        this.vanilla = new EnchantTableRenderer(ctx);
    }

    @Override
    public void render(DisenchanterBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int light, int overlay) {
        EnchantingTableBlockEntity book = be.book();
        if (book != null) vanilla.render(book, partialTick, poseStack, buffers, light, overlay);
    }
}
