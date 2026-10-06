package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
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
        Dialog dialog_ = new Dialog(
                Dialog.blankPane(PicnicBasketSlotsPane.WIDTH, 8),
                new PicnicBasketSlotsPane(),
                Dialog.blankPane(PicnicBasketSlotsPane.WIDTH, 14),
                new PlayerInventoryPane(PicnicBasketMenu.PLAYER_SLOT_LEFT));
        super(menu, inv, title, dialog_.totalWidth(), dialog_.totalHeight());
        dialog = dialog_;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
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
                PicnicBasketMenu.PLAYER_SLOT_LEFT,
                PicnicBasketMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        lastShiftDragSlot = null;
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dialog.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dialog.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) return true;

        if (event.button() == 0 && event.hasShiftDown() && menu.getCarried().isEmpty()) {
            Slot slot = slotAt(event.x(), event.y());
            if (slot != null && slot != lastShiftDragSlot && slot.hasItem()) {
                slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
                lastShiftDragSlot = slot;
            }
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        lastShiftDragSlot = null;
        if (dialog.mouseReleased(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
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
