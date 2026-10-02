package net.bobofraggins.mobfarmingsupplies.shared.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A two-state slider switch (64 × 16): a blank switch image with the knob on the left or right,
 * and a translatable label drawn centred in the remaining space — so the labels can be
 * localised instead of being baked into the art.
 *
 * <p>Convention used by the UIs: the "positive" state (Is, Or, On) has the knob on the right,
 * the other state (Is Not, And, Off) on the left.
 */
public final class ToggleSwitch {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 16;

    private static final Identifier KNOB_LEFT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/toggle_left");
    private static final Identifier KNOB_RIGHT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/toggle_right");

    // Label area (x range, inclusive) inside each image, measured from the art.
    private static final int KNOB_RIGHT_LABEL_MIN = 1;
    private static final int KNOB_RIGHT_LABEL_MAX = 51;
    private static final int KNOB_LEFT_LABEL_MIN = 15;
    private static final int KNOB_LEFT_LABEL_MAX = 62;

    private static final int TEXT_COLOR = 0xFF000000;

    private ToggleSwitch() {}

    public static void draw(GuiGraphicsExtractor g, Font font, int x, int y, boolean knobRight, Component label) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, knobRight ? KNOB_RIGHT : KNOB_LEFT, x, y, WIDTH, HEIGHT);
        int min = knobRight ? KNOB_RIGHT_LABEL_MIN : KNOB_LEFT_LABEL_MIN;
        int max = knobRight ? KNOB_RIGHT_LABEL_MAX : KNOB_LEFT_LABEL_MAX;
        int centreX = x + (min + max + 1) / 2;
        g.text(font, label, centreX - font.width(label) / 2, y + (HEIGHT - font.lineHeight) / 2 + 1, TEXT_COLOR, false);
    }

    public static boolean contains(double px, double py, int x, int y) {
        return px >= x && px < x + WIDTH && py >= y && py < y + HEIGHT;
    }
}
