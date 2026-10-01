package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Logistic Sorter.
 *
 * <p>Layout (within the 176-wide dialog):
 * <ul>
 *   <li>Title bar (17 px), 4 px gap</li>
 *   <li>Side configuration (90 px), 4 px gap</li>
 *   <li>AND / OR toggle (20 px), 4 px gap</li>
 *   <li>Nine Item Filter slots (18 px), 6 px gap</li>
 *   <li>Player inventory 3×9 + hotbar (80 px)</li>
 * </ul>
 * Slot positions live in {@link LogisticSorterMenu} and must match these pane heights.
 */
public class LogisticSorterScreen extends AbstractContainerScreen<LogisticSorterMenu> {

    private final Dialog dialog;
    private final SorterSidesPane sidesPane;

    public LogisticSorterScreen(LogisticSorterMenu menu, Inventory inv, Component title) {
        SorterConfigView config = new SorterConfigView(menu.getPos());
        int w = PlayerInventoryPane.WIDTH;
        SorterSidesPane sides = new SorterSidesPane(config);
        Dialog d = new Dialog(
                Dialog.blankPane(w, 4),
                sides,
                Dialog.blankPane(w, 4),
                new SorterModePane(config),
                Dialog.blankPane(w, 4),
                new FilterSlotsPane(),
                Dialog.blankPane(w, 6),
                new PlayerInventoryPane(LogisticSorterMenu.PLAYER_SLOT_LEFT));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
        sidesPane = sides;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        super.extractContents(g, mouseX, mouseY, partialTick);
        Component tooltip = sidesPane.hoverTooltip();
        if (tooltip != null && menu.getCarried().isEmpty()) g.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Title is drawn by Dialog; suppress the default title/inventory labels.
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }

    // ── Panes ────────────────────────────────────────────────────────────────────

    /** Backgrounds for the nine Item Filter slots. */
    private static final class FilterSlotsPane implements IDialogPane {
        @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
        @Override public int preferredHeight() { return 18; }

        @Override
        public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
            for (int i = 0; i < LogisticSorterBlockEntity.FILTER_SLOTS; i++) {
                PlayerInventoryPane.drawSlotBg(g, LogisticSorterMenu.FILTER_LEFT + i * 18, 0);
            }
        }
    }
}
