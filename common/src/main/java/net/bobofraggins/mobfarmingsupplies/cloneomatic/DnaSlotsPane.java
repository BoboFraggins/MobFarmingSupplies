package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Dialog pane that renders the single-row DNA slot area for the Clone-O-Matic GUI,
 * along with a "DNA Samples to Clone:" label above the slots.
 *
 * <p>Internal layout:
 * <pre>
 *   y =  4  "DNA Samples to Clone:" label
 *   y = 15  nine slot backgrounds in a horizontal row
 * </pre>
 *
 * <p>Pane height = 4 (top pad) + 9 (font) + 2 (gap) + 18 (slot) + 2 (bottom pad) = {@value #HEIGHT} px.
 */
public class DnaSlotsPane implements IDialogPane {

    /** Total pane height in pixels. */
    public static final int HEIGHT = 35;

    private static final int LABEL_Y = 4;
    private static final int SLOT_Y  = 15;

    private static final Component LABEL =
            Component.translatable("container.mobfarmingsupplies.dna_slots_label");

    @Override
    public int preferredWidth() {
        return PlayerInventoryPane.WIDTH;
    }

    @Override
    public int preferredHeight() {
        return HEIGHT;
    }

    @Override
    public void render(
            GuiGraphicsExtractor g, Font font, int width,
            int localMouseX, int localMouseY, float partialTick) {

        g.text(font, LABEL, CloneOMaticMenu.SLOT_LEFT, LABEL_Y, 0xFF404040, false);

        for (int i = 0; i < CloneOMaticBlockEntity.DNA_SLOTS; i++) {
            PlayerInventoryPane.drawSlotBg(g, CloneOMaticMenu.SLOT_LEFT + i * 18, SLOT_Y);
        }
    }
}
