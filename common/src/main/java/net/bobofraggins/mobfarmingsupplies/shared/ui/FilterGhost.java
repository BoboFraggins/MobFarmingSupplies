package net.bobofraggins.mobfarmingsupplies.shared.ui;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;

/**
 * A pale Item Filter (or Blank Filter) drawn in an empty filter slot, so it shows what goes there
 * (the hoppers' and the Logistic Sorter's filter rows, and the Filter Scribing Terminal's slots).
 */
public final class FilterGhost {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/item_filter_ghost.png");
    private static final Identifier BLANK_TEXTURE =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/blank_filter_ghost.png");
    /** The filters' textures are 32 px; they're drawn at a slot's 16. */
    private static final int TEXTURE_SIZE = 32;

    private FilterGhost() {}

    /** Draws the ghost at the slot's item position ({@code x}, {@code y}) if slot {@code index} is empty. */
    public static void drawIfEmpty(GuiGraphicsExtractor g, Container filters, int index, int x, int y) {
        if (filters.getItem(index).isEmpty()) draw(g, x, y);
    }

    /** Draws the ghost at a slot's item position ({@code x}, {@code y}). */
    public static void draw(GuiGraphicsExtractor g, int x, int y) {
        draw(g, TEXTURE, x, y);
    }

    /** Draws a ghost Blank Filter at a slot's item position ({@code x}, {@code y}). */
    public static void drawBlank(GuiGraphicsExtractor g, int x, int y) {
        draw(g, BLANK_TEXTURE, x, y);
    }

    private static void draw(GuiGraphicsExtractor g, Identifier texture, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
