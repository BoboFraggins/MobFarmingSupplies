package net.bobofraggins.mobfarmingsupplies.upgradestation;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Upgrade Station:
 * <pre>
 *   ┌─ Upgrade Station ────────────┐
 *   │            [item]            │
 *   │     [input]  ──▶  [result]   │
 *   ├──────────────────────────────┤
 *   │  Player inventory            │
 *   └──────────────────────────────┘
 * </pre>
 * The input and result show a ghost while empty.
 */
public class UpgradeStationScreen extends AbstractContainerScreen<UpgradeStationMenu> {

    private static final ResourceLocation GHOST =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/blank_filter_ghost.png");

    /** Vanilla's furnace, for its empty arrow. */
    private static final ResourceLocation FURNACE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final int ARROW_U = 79, ARROW_V = 34, ARROW_W = 24, ARROW_H = 17;

    private final Dialog dialog;

    public UpgradeStationScreen(UpgradeStationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, UpgradeStationMenu.SETTINGS_PANE_H),
                new PlayerInventoryPane(UpgradeStationMenu.INV_START_X));
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

        PlayerInventoryPane.drawSlotBg(g, x + UpgradeStationMenu.ITEM_X, y + UpgradeStationMenu.ITEM_Y);
        int rowY = y + UpgradeStationMenu.ROW_Y;
        ghostSlot(g, x + UpgradeStationMenu.INPUT_X, rowY, UpgradeStationBlockEntity.INPUT_SLOT);
        ghostSlot(g, x + UpgradeStationMenu.OUTPUT_X, rowY, UpgradeStationBlockEntity.OUTPUT_SLOT);
        g.blit(FURNACE, x + UpgradeStationMenu.ARROW_X, rowY, ARROW_U, ARROW_V, ARROW_W, ARROW_H, 256, 256);
    }

    /** A slot's background, with the ghost in it while it's empty. */
    private void ghostSlot(GuiGraphics g, int sx, int sy, int slotIndex) {
        PlayerInventoryPane.drawSlotBg(g, sx, sy);
        if (menu.getSlot(slotIndex).getItem().isEmpty()) {
            g.blit(GHOST, sx, sy, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // The Dialog draws the title.
    }
}
