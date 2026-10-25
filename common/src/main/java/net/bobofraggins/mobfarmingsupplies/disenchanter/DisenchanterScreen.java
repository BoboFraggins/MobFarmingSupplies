package net.bobofraggins.mobfarmingsupplies.disenchanter;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the XP Powered Disenchanter: the experience gauge, the item being disenchanted on
 * top, and a row of books in (with a ghost book while empty), an arrow filling as an enchantment
 * comes off, and the enchanted books out.
 */
public class DisenchanterScreen extends AbstractContainerScreen<DisenchanterMenu> {

    private static final ResourceLocation GHOST_BOOK =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/book_ghost.png");

    /** Vanilla's furnace: its empty arrow (in the screen texture) and the arrow that fills it (a sprite). */
    private static final ResourceLocation FURNACE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation ARROW_FILL = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_U = 79, ARROW_V = 34, ARROW_W = 24, ARROW_H = 17;

    /** XP Juice's green, and a lighter stripe down the gauge's left side. */
    private static final int GAUGE_FILL = 0xFF2FCC12;
    private static final int GAUGE_SHINE = 0xFF6CFF52;

    private final Dialog dialog;

    public DisenchanterScreen(DisenchanterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, DisenchanterMenu.SETTINGS_PANE_H),
                new PlayerInventoryPane(DisenchanterMenu.INV_START_X));
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
        PlayerInventoryPane.drawSlotBg(g, x + DisenchanterMenu.ITEM_X, y + DisenchanterMenu.ITEM_Y);
        PlayerInventoryPane.drawSlotBg(g, x + DisenchanterMenu.BOOK_X, y + DisenchanterMenu.ROW_Y);
        PlayerInventoryPane.drawSlotBg(g, x + DisenchanterMenu.OUTPUT_X, y + DisenchanterMenu.ROW_Y);
        if (menu.getSlot(DisenchanterBlockEntity.BOOK).getItem().isEmpty()) {
            g.blit(GHOST_BOOK, x + DisenchanterMenu.BOOK_X, y + DisenchanterMenu.ROW_Y,
                    0, 0, 16, 16, 16, 16);
        }
        int arrowX = x + DisenchanterMenu.ARROW_X;
        int arrowY = y + DisenchanterMenu.ROW_Y;
        g.blit(FURNACE, arrowX, arrowY, ARROW_U, ARROW_V, ARROW_W, ARROW_H, 256, 256);
        int filled = Mth.ceil(menu.progress() * ARROW_W);
        if (filled > 0) {
            g.blitSprite(ARROW_FILL, ARROW_W, 16, 0, 0, arrowX, arrowY, filled, 16);
        }
        drawGauge(g, x + DisenchanterMenu.GAUGE_X, y + DisenchanterMenu.GAUGE_Y);
    }

    /** The experience gauge: a sunken bar filling from the bottom in XP Juice green. */
    private void drawGauge(GuiGraphics g, int gx, int gy) {
        int w = DisenchanterMenu.GAUGE_W, h = DisenchanterMenu.GAUGE_H;
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
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        int gx = leftPos + DisenchanterMenu.GAUGE_X, gy = topPos + DisenchanterMenu.GAUGE_Y;
        if (mouseX >= gx && mouseX < gx + DisenchanterMenu.GAUGE_W && mouseY >= gy && mouseY < gy + DisenchanterMenu.GAUGE_H) {
            g.renderTooltip(font, Component.translatable("gui.mobfarmingsupplies.incubator.experience",
                    menu.experience(), menu.capacity()), mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // The Dialog draws the title.
    }
}
