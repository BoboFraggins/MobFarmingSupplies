package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Dialog pane that renders the background of the Absorption Hopper's 27 item slots
 * (3 rows × 9 columns) plus the fluid gauge outline.
 *
 * <p>Local (0, 0) is the top-left of the first slot row. Actual item stacks are drawn
 * by {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}.
 */
public class HopperSlotsPane implements IDialogPane {

    private static final int ROWS = 3;
    private static final int COLS = 9;

    /** Left margin for the slot grid (matching AbsorptionHopperMenu.SLOT_LEFT). */
    private static final int SLOT_LEFT = 8;

    /** Height: 3 slot rows (each 18 px). */
    public static final int HEIGHT = ROWS * 18; // 54

    /** Gap in pixels between the right edge of the slots and the fluid gauge. */
    static final int GAUGE_GAP = 3;

    /** X position of the fluid gauge (right of the slot grid + gap). */
    static final int GAUGE_X = SLOT_LEFT + COLS * 18 + GAUGE_GAP; // 173

    /** Width of the fluid gauge. */
    static final int GAUGE_W = 15;

    /** Height of the fluid gauge (matches HEIGHT). */
    static final int GAUGE_H = HEIGHT; // 54

    /** Right margin after the gauge. */
    private static final int RIGHT_MARGIN = 8;

    /** Total pane width: left margin + slots + gap + gauge + right margin. */
    public static final int WIDTH = SLOT_LEFT + COLS * 18 + GAUGE_GAP + GAUGE_W + RIGHT_MARGIN; // 196

    private static final int GAUGE_BG     = 0xFF4A4A4A;
    private static final int GAUGE_BORDER = 0xFF222222;

    @Override
    public int preferredWidth() { return WIDTH; }

    @Override
    public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(
            GuiGraphicsExtractor g, Font font, int width, int localMouseX, int localMouseY, float partialTick) {
        // Slot backgrounds
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                PlayerInventoryPane.drawSlotBg(g, SLOT_LEFT + col * 18, row * 18);
            }
        }

        // Fluid gauge background
        g.fill(GAUGE_X - 1,          -1,              GAUGE_X + GAUGE_W + 1, GAUGE_H + 1, GAUGE_BORDER);
        g.fill(GAUGE_X,              0,               GAUGE_X + GAUGE_W,     GAUGE_H,     GAUGE_BG);
    }
}
