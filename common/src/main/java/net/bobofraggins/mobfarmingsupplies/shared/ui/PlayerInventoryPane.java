package net.bobofraggins.mobfarmingsupplies.shared.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Dialog pane that renders the player inventory and hotbar slot backgrounds.
 *
 * <p>Local coordinate origin (0, 0) sits at the top-left of the first inventory row.
 * Vanilla {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen} renders
 * the actual item stacks on top of these backgrounds.
 */
public class PlayerInventoryPane implements IDialogPane {

    private static final int SLOT_SIZE = 18;
    private static final int COLS = 9;
    private static final int INV_ROWS = 3;
    private static final int HOTBAR_GAP = 4;

    private static final int DARK  = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int FILL  = 0xFF8B8B8B;

    /** Standard width covering all 9 columns. */
    public static final int WIDTH = 176;

    /** Height: 3 inventory rows + gap + hotbar row + bottom gap. */
    public static final int HEIGHT = INV_ROWS * SLOT_SIZE + HOTBAR_GAP + SLOT_SIZE + HOTBAR_GAP; // 80

    private final int invX;
    private final int hotbarY;

    public PlayerInventoryPane() {
        this(20);
    }

    public PlayerInventoryPane(int leftMargin) {
        this.invX = leftMargin;
        this.hotbarY = INV_ROWS * SLOT_SIZE + HOTBAR_GAP;
    }

    @Override
    public int preferredWidth() {
        return WIDTH;
    }

    @Override
    public int preferredHeight() {
        return HEIGHT;
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics, Font font, int width, int localMouseX, int localMouseY, float partialTick) {
        for (int row = 0; row < INV_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                drawSlotBg(graphics, invX + col * SLOT_SIZE, row * SLOT_SIZE);
            }
        }
        for (int col = 0; col < COLS; col++) {
            drawSlotBg(graphics, invX + col * SLOT_SIZE, hotbarY);
        }
    }

    public static void drawSlotBg(GuiGraphicsExtractor graphics, int sx, int sy) {
        graphics.fill(sx,      sy,      sx + 16, sy + 1,  DARK);
        graphics.fill(sx,      sy + 1,  sx + 1,  sy + 16, DARK);
        graphics.fill(sx,      sy + 16, sx + 17, sy + 17, LIGHT);
        graphics.fill(sx + 16, sy,      sx + 17, sy + 16, LIGHT);
        graphics.fill(sx + 1,  sy + 1,  sx + 16, sy + 16, FILL);
    }
}
