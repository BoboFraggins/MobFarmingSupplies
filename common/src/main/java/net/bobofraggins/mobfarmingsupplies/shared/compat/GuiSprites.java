package net.bobofraggins.mobfarmingsupplies.shared.compat;

import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.20.1 has no GUI sprite atlas ({@code GuiGraphics.blitSprite} came in 1.20.2): a sprite id
 * {@code ns:path} is drawn from {@code ns:textures/gui/sprites/path.png}, and the few vanilla sprites
 * this mod uses from the spot on the 1.20.1 texture they were cut from.
 */
public final class GuiSprites {

    private record Region(ResourceLocation texture, int u, int v, int texWidth, int texHeight) {}

    private static final Map<ResourceLocation, Region> VANILLA = Map.of(
            new ResourceLocation("container/furnace/burn_progress"),
            new Region(new ResourceLocation("textures/gui/container/furnace.png"), 176, 14, 256, 256));

    private GuiSprites() {}

    /** Draws the whole sprite stretched over {@code width} x {@code height}. */
    public static void blitSprite(GuiGraphics g, ResourceLocation sprite, int x, int y, int width, int height) {
        Region vanilla = VANILLA.get(sprite);
        if (vanilla != null) {
            g.blit(vanilla.texture(), x, y, vanilla.u(), vanilla.v(), width, height, vanilla.texWidth(), vanilla.texHeight());
            return;
        }
        g.blit(texture(sprite), x, y, width, height, 0f, 0f, width, height, width, height);
    }

    /** Draws part of a {@code spriteWidth} x {@code spriteHeight} sprite: {@code width} x {@code height} from ({@code u}, {@code v}). */
    public static void blitSprite(GuiGraphics g, ResourceLocation sprite, int spriteWidth, int spriteHeight, int u, int v,
                                  int x, int y, int width, int height) {
        Region vanilla = VANILLA.get(sprite);
        if (vanilla != null) {
            g.blit(vanilla.texture(), x, y, vanilla.u() + u, vanilla.v() + v, width, height,
                    vanilla.texWidth(), vanilla.texHeight());
            return;
        }
        g.blit(texture(sprite), x, y, width, height, (float) u, (float) v, width, height, spriteWidth, spriteHeight);
    }

    private static ResourceLocation texture(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }
}
