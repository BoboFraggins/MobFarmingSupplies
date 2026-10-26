package net.bobofraggins.mobfarmingsupplies.shared.ui;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;

/**
 * A pale Item Filter drawn in an empty filter slot, so it shows what goes there (the hoppers' and
 * the Logistic Sorter's filter rows).
 */
public final class FilterGhost {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/item_filter_ghost.png");
    /** The Item Filter's texture is 32 px; it's drawn at a slot's 16. */
    private static final int TEXTURE_SIZE = 32;

    private FilterGhost() {}

    /** Draws the ghost at the slot's item position ({@code x}, {@code y}) if slot {@code index} is empty. */
    public static void drawIfEmpty(GuiGraphicsExtractor g, Container filters, int index, int x, int y) {
        if (filters.getItem(index).isEmpty()) draw(g, x, y);
    }

    /** Draws the ghost at a slot's item position ({@code x}, {@code y}). */
    public static void draw(GuiGraphicsExtractor g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
