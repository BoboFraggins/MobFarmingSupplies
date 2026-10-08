package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.bobofraggins.mobfarmingsupplies.shared.ui.SideGridPane;
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
 * Screen for the Omnidirectional Hopper.
 *
 * <p>Layout (within the 176-wide dialog):
 * <ul>
 *   <li>Title bar (17 px), 4 px gap</li>
 *   <li>Side configuration (90 px), 4 px gap</li>
 *   <li>AND / OR toggle (20 px), 4 px gap</li>
 *   <li>Nine Item Filter slots (18 px), 6 px gap</li>
 *   <li>Player inventory 3×9 + hotbar (80 px)</li>
 * </ul>
 * Slot positions live in {@link OmniHopperMenu} and must match these pane heights.
 */
public class OmniHopperScreen<M extends OmniHopperMenu> extends AbstractContainerScreen<M> {

    private final Dialog dialog;
    private final SideGridPane sidesPane;

    public OmniHopperScreen(M menu, Inventory inv, Component title) {
        HopperConfigView config = new HopperConfigView(menu.getPos());
        int w = PlayerInventoryPane.WIDTH;
        SideGridPane sides = new SideGridPane(menu.getPos(),
                Component.translatable("container.mobfarmingsupplies.logistic_sorter.connections"),
                config);
        Dialog d = new Dialog(
                Dialog.blankPane(w, 4),
                sides,
                Dialog.blankPane(w, 4),
                new HopperModePane(config, menu.getFilters()),
                Dialog.blankPane(w, 4),
                new FilterSlotsPane(),
                Dialog.blankPane(w, 6),
                new PlayerInventoryPane(OmniHopperMenu.PLAYER_SLOT_LEFT));
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
            for (int i = 0; i < OmniHopperBlockEntity.FILTER_SLOTS; i++) {
                PlayerInventoryPane.drawSlotBg(g, OmniHopperMenu.FILTER_LEFT + i * 18, 0);
            }
        }
    }
}
