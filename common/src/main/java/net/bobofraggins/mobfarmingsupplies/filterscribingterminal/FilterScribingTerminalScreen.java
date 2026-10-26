package net.bobofraggins.mobfarmingsupplies.filterscribingterminal;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatcher;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatchers;
import net.bobofraggins.mobfarmingsupplies.network.SetScribingStatePacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.FilterGhost;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

/**
 * Screen for the Filter Scribing Terminal.
 *
 * <p>Layout (176 × 198 px):
 * <pre>
 *   ┌─ Dialog title bar (17 px) ──────────┐
 *   │              [ghost]                │
 *   │ [Blank]     [Is|Is Not]   [Filter]  │  y = 57
 *   │             ┌ list ───┐             │
 *   │             └─────────┘             │
 *   ├──────────── controls (96 px) ───────┤  y = 113
 *   │  Player inventory (3 rows + hotbar) │
 *   └─────────────────────────────────────┘
 * </pre>
 */
public class FilterScribingTerminalScreen extends AbstractContainerScreen<FilterScribingTerminalMenu> {

    private static final int PANE_CONTROLS = 0;

    private final Dialog dialog;
    private final ScribingControlsPane controls;

    public FilterScribingTerminalScreen(FilterScribingTerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        ScribingControlsPane c = new ScribingControlsPane(
                menu::getCarried,
                item -> ItemMatchers.forItem(item, Minecraft.getInstance().level.registryAccess()).stream().map(ItemMatcher::listLabel).toList(),
                pane -> NetworkManager.sendToServer(new SetScribingStatePacket(
                        menu.containerId, pane.getGhostItem(), pane.isNot(), pane.getSelectedRow())));
        Dialog d = new Dialog(c, new PlayerInventoryPane(FilterScribingTerminalMenu.INV_START_X));
        dialog = d;
        controls = c;
        imageWidth = d.totalWidth();
        imageHeight = d.totalHeight();
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Ghost slot access (used by the JEI ghost-ingredient handler) ──────────────

    /** Screen-space area of the ghost slot's item, as a JEI drop target. */
    public Rect2i getGhostSlotArea() {
        return new Rect2i(
                leftPos + ScribingControlsPane.ghostItemX(),
                dialog.getPaneAbsY(PANE_CONTROLS) + ScribingControlsPane.ghostItemY(),
                16, 16);
    }

    public void setGhostItem(ItemStack stack) {
        controls.setGhostItem(stack);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        ItemStack ghost = controls.getGhostItem();
        if (!ghost.isEmpty() && menu.getCarried().isEmpty() && getGhostSlotArea().contains(mouseX, mouseY)) {
            g.renderTooltip(font, ghost, mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        int inputX = leftPos + FilterScribingTerminalMenu.INPUT_X;
        int outputX = leftPos + FilterScribingTerminalMenu.OUTPUT_X;
        int slotY = topPos + FilterScribingTerminalMenu.SLOT_Y;
        drawSlot(g, inputX, slotY);
        drawSlot(g, outputX, slotY);
        // Ghosts while empty: a Blank Filter goes in, an Item Filter comes out.
        if (!menu.getSlot(FilterScribingTerminalMenu.INPUT_SLOT).hasItem()) FilterGhost.drawBlank(g, inputX, slotY);
        if (!menu.getSlot(FilterScribingTerminalMenu.OUTPUT_SLOT).hasItem()) FilterGhost.draw(g, outputX, slotY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Title is drawn by Dialog; suppress the default title/inventory labels.
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dialog.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dialog.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dialog.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dialog.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────────

    /** Draws a terminal slot frame; also used by the JEI Filter Scribing recipe view. */
    public static void drawSlot(GuiGraphics g, int sx, int sy) {
        g.fill(sx,      sy,      sx + 16, sy + 1,  0xFF373737); // top
        g.fill(sx,      sy + 1,  sx + 1,  sy + 16, 0xFF373737); // left
        g.fill(sx,      sy + 16, sx + 17, sy + 17, 0xFFFFFFFF); // bottom
        g.fill(sx + 16, sy,      sx + 17, sy + 16, 0xFFFFFFFF); // right
        g.fill(sx + 1,  sy + 1,  sx + 16, sy + 16, 0xFF8B8B8B); // interior
    }
}
