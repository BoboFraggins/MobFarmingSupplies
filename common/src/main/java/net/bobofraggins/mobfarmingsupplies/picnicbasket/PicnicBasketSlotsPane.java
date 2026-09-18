package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Dialog pane that renders the background of the Picnic Basket's 54 item slots
 * (6 rows × 9 columns). Local (0, 0) is the top-left of the first slot row. Actual item
 * stacks are drawn by {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}.
 */
public class PicnicBasketSlotsPane implements IDialogPane {

    private static final int ROWS = 6;
    private static final int COLS = 9;

    public static final int SLOT_LEFT = 7;
    public static final int HEIGHT = ROWS * 18; // 108
    public static final int WIDTH = SLOT_LEFT * 2 + COLS * 18; // 176

    @Override
    public int preferredWidth() { return WIDTH; }

    @Override
    public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(
            GuiGraphicsExtractor g, Font font, int width, int localMouseX, int localMouseY, float partialTick) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                PlayerInventoryPane.drawSlotBg(g, SLOT_LEFT + col * 18, row * 18);
            }
        }
    }
}
