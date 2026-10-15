package net.bobofraggins.mobfarmingsupplies.shared.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The items drawn by a {@link SpecialModelRenderer}. The loaders hand each of these items
 * {@link #itemRenderer()} (NeoForge through {@code IClientItemExtensions}, Fabric through
 * {@code BuiltinItemRendererRegistry}), which looks the renderer up by item.
 */
public final class SpecialItemRenderers {

    private static final Map<Item, SpecialModelRenderer<?>> RENDERERS = new LinkedHashMap<>();
    @Nullable private static BlockEntityWithoutLevelRenderer itemRenderer;

    private SpecialItemRenderers() {}

    public static void register(Item item, SpecialModelRenderer<?> renderer) {
        RENDERERS.put(item, renderer);
    }

    public static Map<Item, SpecialModelRenderer<?>> all() {
        return Collections.unmodifiableMap(RENDERERS);
    }

    /** Draws {@code stack} with its registered renderer, if it has one. */
    public static void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                              MultiBufferSource buffers, int light, int overlay) {
        SpecialModelRenderer<?> renderer = RENDERERS.get(stack.getItem());
        if (renderer != null) renderer.render(stack, context, poseStack, buffers, light, overlay);
    }

    /** The one item renderer for every registered item, made on first use (after the client starts). */
    public static BlockEntityWithoutLevelRenderer itemRenderer() {
        if (itemRenderer == null) {
            Minecraft mc = Minecraft.getInstance();
            itemRenderer = new BlockEntityWithoutLevelRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels()) {
                @Override
                public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                                         MultiBufferSource buffers, int light, int overlay) {
                    render(stack, context, poseStack, buffers, light, overlay);
                }
            };
        }
        return itemRenderer;
    }
}
