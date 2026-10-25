package net.bobofraggins.mobfarmingsupplies.xpanvil;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the XP Powered Anvil: the experience gauge, then the item being repaired, an arrow
 * filling as it mends, and where it goes once whole. Laid out like a row of the XP Powered
 * Incubator.
 */
public class XpAnvilScreen extends AbstractContainerScreen<XpAnvilMenu> {

    /** Vanilla's furnace: its empty arrow (in the screen texture) and the arrow that fills it (a sprite). */
    private static final Identifier FURNACE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier ARROW_FILL = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_U = 79, ARROW_V = 34, ARROW_W = 24, ARROW_H = 17;

    /** XP Juice's green, and a lighter stripe down the gauge's left side. */
    private static final int GAUGE_FILL = 0xFF2FCC12;
    private static final int GAUGE_SHINE = 0xFF6CFF52;

    private final Dialog dialog;

    public XpAnvilScreen(XpAnvilMenu menu, Inventory inv, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, XpAnvilMenu.SETTINGS_PANE_H),
                new PlayerInventoryPane(XpAnvilMenu.INV_START_X));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        PlayerInventoryPane.drawSlotBg(g, x + XpAnvilMenu.INPUT_X, y + XpAnvilMenu.ROW_Y);
        PlayerInventoryPane.drawSlotBg(g, x + XpAnvilMenu.OUTPUT_X, y + XpAnvilMenu.ROW_Y);
        int arrowX = x + XpAnvilMenu.ARROW_X;
        int arrowY = y + XpAnvilMenu.ROW_Y;
        g.blit(RenderPipelines.GUI_TEXTURED, FURNACE, arrowX, arrowY, ARROW_U, ARROW_V, ARROW_W, ARROW_H, 256, 256);
        int filled = Mth.ceil(menu.progress() * ARROW_W);
        if (filled > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, ARROW_FILL, ARROW_W, 16, 0, 0, arrowX, arrowY, filled, 16);
        }
        drawGauge(g, x + XpAnvilMenu.GAUGE_X, y + XpAnvilMenu.GAUGE_Y);
        super.extractContents(g, mouseX, mouseY, partialTick);
    }

    /** The experience gauge: a sunken bar filling from the bottom in XP Juice green. */
    private void drawGauge(GuiGraphicsExtractor g, int gx, int gy) {
        int w = XpAnvilMenu.GAUGE_W, h = XpAnvilMenu.GAUGE_H;
        g.fill(gx, gy, gx + w - 1, gy + 1, 0xFF373737);
        g.fill(gx, gy + 1, gx + 1, gy + h - 1, 0xFF373737);
        g.fill(gx, gy + h - 1, gx + w, gy + h, 0xFFFFFFFF);
        g.fill(gx + w - 1, gy, gx + w, gy + h - 1, 0xFFFFFFFF);
        g.fill(gx + 1, gy + 1, gx + w - 1, gy + h - 1, 0xFF2B2B2B);
        int inside = h - 2;
        int filled = (int) Math.round((double) menu.experience() / menu.capacity() * inside);
        if (filled > 0) {
            int top = gy + 1 + inside - Math.min(filled, inside);
            g.fill(gx + 1, top, gx + w - 1, gy + h - 1, GAUGE_FILL);
            g.fill(gx + 1, top, gx + 3, gy + h - 1, GAUGE_SHINE);
        }
    }

    /** The gauge's amount, on hover. */
    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int gx = leftPos + XpAnvilMenu.GAUGE_X, gy = topPos + XpAnvilMenu.GAUGE_Y;
        if (mouseX >= gx && mouseX < gx + XpAnvilMenu.GAUGE_W && mouseY >= gy && mouseY < gy + XpAnvilMenu.GAUGE_H) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.mobfarmingsupplies.incubator.experience",
                    menu.experience(), menu.capacity()), mouseX, mouseY);
            return;
        }
        super.extractTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // The Dialog draws the title.
    }
}
