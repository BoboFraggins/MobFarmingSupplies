package net.bobofraggins.mobfarmingsupplies.fan;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Dialog pane that renders the "Upgrades" header and three Fan upgrade slot backgrounds in a centred row.
 *
 * <p>Pane height = 10 (header) + 6 (top pad) + 18 (one row) + 2 (bottom pad) = {@value #HEIGHT} px.
 */
public class FanUpgradeSlotsPane implements IDialogPane {

    /** Total pane height in pixels. */
    public static final int HEIGHT = 10 + 6 + 18 + 2; // = 36

    private static final int HEADER_Y = 4;
    private static final int ROW_Y    = 16;

    @Override
    public int preferredWidth() {
        return PlayerInventoryPane.WIDTH;
    }

    @Override
    public int preferredHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width,
                       int localMouseX, int localMouseY, float partialTick) {
        g.text(font, net.minecraft.network.chat.Component.literal("Upgrades"),
                8, HEADER_Y, 0xFF404040, false);
        for (int i = 0; i < FanBlockEntity.UPGRADE_SLOTS; i++) {
            PlayerInventoryPane.drawSlotBg(g, FanMenu.UPGRADE_X_START + i * 18, ROW_Y);
        }
    }
}
