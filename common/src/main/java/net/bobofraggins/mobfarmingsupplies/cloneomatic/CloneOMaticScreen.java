package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

    private static final Identifier GHOST_DNA_SAMPLE =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/dna_sample_ghost.png");

    private final Dialog dialog;

    public CloneOMaticScreen(CloneOMaticMenu menu, Inventory inv, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new DnaSlotsPane(),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new PlayerInventoryPane(CloneOMaticMenu.SLOT_LEFT));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    // ── Init ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        for (int i = 0; i < CloneOMaticBlockEntity.DNA_SLOTS; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) {
                g.blit(RenderPipelines.GUI_TEXTURED, GHOST_DNA_SAMPLE,
                        leftPos + CloneOMaticMenu.SLOT_LEFT + i * CloneOMaticMenu.DNA_SLOT_STRIDE,
                        topPos + CloneOMaticMenu.DNA_SLOT_TOP,
                        0, 0, 16, 16, 16, 16);
            }
        }

        super.extractContents(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font,
                Component.translatable("container.inventory"),
                CloneOMaticMenu.SLOT_LEFT,
                CloneOMaticMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ────────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }
}
