package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Dialog pane that renders the "Pickup Zone:" header and X/Y/Z axis labels for the
 * Absorption Hopper's offset controls.
 *
 * <p>The actual ± buttons are added as vanilla {@link net.minecraft.client.gui.components.Button}
 * widgets in {@link AbsorptionHopperScreen#init()}.
 */
public class OffsetControlsPane implements IDialogPane {

    public static final int WIDTH  = 176;
    public static final int HEIGHT = 34;

    /** Y of the axis letters (pane-local), below the "Pickup Zone:" header. */
    static final int LABEL_Y = 20;

    /** X positions for the axis letters, in the 2px-padded gap between − and + buttons. */
    private static final int AXIS_LETTER_X_X = 44;
    private static final int AXIS_LETTER_X_Y = 100;
    private static final int AXIS_LETTER_X_Z = 156;

    private static final int TEXT_COLOR = 0xFF404040;

    @Override
    public int preferredWidth()  { return WIDTH; }

    @Override
    public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(
            GuiGraphicsExtractor g, Font font, int width, int localMouseX, int localMouseY, float partialTick) {
        g.text(font, Component.literal("Pickup Zone:"), 8, 4, TEXT_COLOR, false);
        g.text(font, Component.literal("X"), AXIS_LETTER_X_X, LABEL_Y, TEXT_COLOR, false);
        g.text(font, Component.literal("Y"), AXIS_LETTER_X_Y, LABEL_Y, TEXT_COLOR, false);
        g.text(font, Component.literal("Z"), AXIS_LETTER_X_Z, LABEL_Y, TEXT_COLOR, false);
    }
}
