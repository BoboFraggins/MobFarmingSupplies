package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import org.jetbrains.annotations.Nullable;

/**
 * Screen for the Picnic Basket.
 *
 * <p>Layout (within the 176-wide dialog):
 * <ul>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px gap</li>
 *   <li>6 rows × 9 basket slots (108 px)</li>
 *   <li>14 px gap</li>
 *   <li>Player inventory 3×9 + hotbar (80 px)</li>
 * </ul>
 *
 * <p>The player inventory grid shares the same left margin as the basket slots
 * ({@link PicnicBasketMenu#PLAYER_SLOT_LEFT}) so both 9-wide grids line up.
 */
public class PicnicBasketScreen extends AbstractContainerScreen<PicnicBasketMenu> {

    private final Dialog dialog;

    /**
     * Last slot a shift-drag gesture quick-moved, so dragging across many slots quick-moves
     * each one exactly once. Vanilla has no built-in "shift + click-drag" quick-move — only
     * shift + double-click on a single slot — so this replicates it ourselves for both the
     * basket and player inventory grids.
     */
    @Nullable private Slot lastShiftDragSlot;

    public PicnicBasketScreen(PicnicBasketMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog dialog_ = new Dialog(
                Dialog.blankPane(PicnicBasketSlotsPane.WIDTH, 8),
                new PicnicBasketSlotsPane(),
                Dialog.blankPane(PicnicBasketSlotsPane.WIDTH, 14),
                new PlayerInventoryPane(PicnicBasketMenu.PLAYER_SLOT_LEFT));
        dialog = dialog_;
        imageWidth = dialog_.totalWidth();
        imageHeight = dialog_.totalHeight();
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
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
                PicnicBasketMenu.PLAYER_SLOT_LEFT,
                PicnicBasketMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastShiftDragSlot = null;
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

        if (button == 0 && hasShiftDown() && menu.getCarried().isEmpty()) {
            Slot slot = slotAt(mouseX, mouseY);
            if (slot != null && slot != lastShiftDragSlot && slot.hasItem()) {
                slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                lastShiftDragSlot = slot;
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastShiftDragSlot = null;
        if (dialog.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Nullable
    private Slot slotAt(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }
}
