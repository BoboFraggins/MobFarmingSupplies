package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI screen for the Clone-O-Matic.
 *
 * <p>Layout (176 px wide):
 * <ol>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px blank gap</li>
 *   <li>{@link DnaSlotsPane} — label + single row of 9 DNA sample slots (35 px)</li>
 *   <li>8 px blank gap</li>
 *   <li>{@link PlayerInventoryPane} — 3×9 inventory + hotbar (80 px)</li>
 *   <li>5 px bottom border padding</li>
 * </ol>
 *
 * <p>Total height = 17 + 8 + 35 + 8 + 80 + 5 = 153 px.
 */
public class CloneOMaticScreen extends AbstractContainerScreen<CloneOMaticMenu> {

    private static final ResourceLocation GHOST_DNA_SAMPLE =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/dna_sample_ghost.png");

    private final Dialog dialog;

    public CloneOMaticScreen(CloneOMaticMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new DnaSlotsPane(),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new PlayerInventoryPane(CloneOMaticMenu.SLOT_LEFT));
        dialog = d;
        imageWidth = d.totalWidth();
        imageHeight = d.totalHeight();
    }

    // ── Init ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        for (int i = 0; i < CloneOMaticBlockEntity.DNA_SLOTS; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) {
                g.blit(GHOST_DNA_SAMPLE,
                        leftPos + CloneOMaticMenu.SLOT_LEFT + i * CloneOMaticMenu.DNA_SLOT_STRIDE,
                        topPos + CloneOMaticMenu.DNA_SLOT_TOP,
                        0, 0, 16, 16, 16, 16);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font,
                Component.translatable("container.inventory"),
                CloneOMaticMenu.SLOT_LEFT,
                CloneOMaticMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ────────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dialog.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
