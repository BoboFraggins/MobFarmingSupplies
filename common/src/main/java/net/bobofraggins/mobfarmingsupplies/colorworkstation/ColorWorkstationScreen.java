package net.bobofraggins.mobfarmingsupplies.colorworkstation;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Screen for the Color Workstation: the Essence of Color gauge (its colour cycling, as on the
 * Essence Hopper), then the 4×4 grid of dyes. A dye the buffer can't pay for shows as a ghost.
 */
public class ColorWorkstationScreen extends AbstractContainerScreen<ColorWorkstationMenu> {

    /** Washes a ghost dye out, like vanilla's recipe-book ghosts but stronger. */
    private static final int GHOST_WASH = 0xA08B8B8B;

    private final Dialog dialog;

    public ColorWorkstationScreen(ColorWorkstationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, ColorWorkstationMenu.SETTINGS_PANE_H),
                new PlayerInventoryPane(ColorWorkstationMenu.INV_START_X));
        imageWidth = d.totalWidth();
        imageHeight = d.totalHeight();
        dialog = d;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        for (int i = 0; i < ColorWorkstationBlockEntity.DYES.size(); i++) {
            int sx = x + ColorWorkstationMenu.GRID_X + (i % 4) * 18;
            int sy = y + ColorWorkstationMenu.GRID_Y + (i / 4) * 18;
            PlayerInventoryPane.drawSlotBg(g, sx, sy);
            if (menu.getSlot(i).getItem().isEmpty()) {
                g.renderFakeItem(new ItemStack(ColorWorkstationBlockEntity.DYES.get(i)), sx, sy);
                // Over the item, which draws in front of the background.
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                g.fill(sx, sy, sx + 16, sy + 16, GHOST_WASH);
                g.pose().popPose();
            }
        }
        drawGauge(g, x + ColorWorkstationMenu.GAUGE_X, y + ColorWorkstationMenu.GAUGE_Y);
    }

    /** The buffer: a sunken bar filling from the bottom in Essence of Color's cycling hue. */
    private void drawGauge(GuiGraphics g, int gx, int gy) {
        int w = ColorWorkstationMenu.GAUGE_W, h = ColorWorkstationMenu.GAUGE_H;
        g.fill(gx, gy, gx + w - 1, gy + 1, 0xFF373737);
        g.fill(gx, gy + 1, gx + 1, gy + h - 1, 0xFF373737);
        g.fill(gx, gy + h - 1, gx + w, gy + h, 0xFFFFFFFF);
        g.fill(gx + w - 1, gy, gx + w, gy + h - 1, 0xFFFFFFFF);
        g.fill(gx + 1, gy + 1, gx + w - 1, gy + h - 1, 0xFF2B2B2B);
        int inside = h - 2;
        int filled = (int) Math.round((double) menu.essence() / menu.capacity() * inside);
        if (menu.essence() > 0) filled = Math.max(filled, 1);
        if (filled > 0) {
            int top = gy + 1 + inside - Math.min(filled, inside);
            g.fill(gx + 1, top, gx + w - 1, gy + h - 1, Essence.COLOR.gaugeColor());
            g.fill(gx + 1, top, gx + 3, gy + h - 1, 0x40FFFFFF);
        }
    }

    /** The buffer's amount and how many dyes it makes, on hover. */
    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        int gx = leftPos + ColorWorkstationMenu.GAUGE_X, gy = topPos + ColorWorkstationMenu.GAUGE_Y;
        if (mouseX >= gx && mouseX < gx + ColorWorkstationMenu.GAUGE_W
                && mouseY >= gy && mouseY < gy + ColorWorkstationMenu.GAUGE_H) {
            g.renderComponentTooltip(font, java.util.List.of(
                    Component.translatable("gui.mobfarmingsupplies.color_workstation.essence",
                            menu.essence(), menu.capacity()),
                    Component.translatable("gui.mobfarmingsupplies.color_workstation.dyes",
                            menu.essence() / ColorWorkstationBlockEntity.MB_PER_DYE,
                            ColorWorkstationBlockEntity.MB_PER_DYE)
                            .withStyle(net.minecraft.ChatFormatting.GRAY)),
                    mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // The Dialog draws the title.
    }
}
