package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.network.SetInhibitorOffsetPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.Nullable;

/**
 * Settings screen for the Ender Inhibitor.
 *
 * <p>Layout (within the 176-wide dialog):
 * <ul>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px gap</li>
 *   <li>Offset controls row (34 px)</li>
 *   <li>4 px gap</li>
 *   <li>Show/Hide Area toggle row (20 px)</li>
 *   <li>8 px gap</li>
 * </ul>
 *
 * <p>No item slots — this is a pure settings dialog.
 */
public class EnderInhibitorScreen extends AbstractContainerScreen<EnderInhibitorMenu> {

    // ── Pane indices in the dialog ──────────────────────────────────────────────
    private static final int PANE_TOP_GAP   = 0;
    private static final int PANE_OFFSETS   = 1;
    private static final int PANE_GAP1      = 2;
    private static final int PANE_SHOW_AREA = 3;
    private static final int PANE_BOTTOM_GAP = 4;

    private static final int SHOW_AREA_PANE_H   = 20;
    private static final int SHOW_AREA_BUTTON_W = 90;

    private final Dialog dialog;

    public EnderInhibitorScreen(EnderInhibitorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        IDialogPane offsetsPane = new EnderInhibitorSettingsPane();
        Dialog dialog_ = new Dialog(
                Dialog.blankPane(EnderInhibitorSettingsPane.WIDTH, 8),              // PANE_TOP_GAP
                offsetsPane,                                                          // PANE_OFFSETS
                Dialog.blankPane(EnderInhibitorSettingsPane.WIDTH, 4),              // PANE_GAP1
                Dialog.blankPane(EnderInhibitorSettingsPane.WIDTH, SHOW_AREA_PANE_H), // PANE_SHOW_AREA
                Dialog.blankPane(EnderInhibitorSettingsPane.WIDTH, 8));             // PANE_BOTTOM_GAP
        dialog = dialog_;
        imageWidth = dialog_.totalWidth();
        imageHeight = dialog_.totalHeight();
    }

    // ── Init ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);

        // Show Area toggle
        int saY = dialog.getPaneAbsY(PANE_SHOW_AREA);
        int saX = leftPos + (dialog.totalWidth() - SHOW_AREA_BUTTON_W) / 2;
        EnderInhibitorBlockEntity initBe = getClientBE();
        boolean initShowArea = initBe != null && initBe.showArea;
        addRenderableWidget(Button.builder(showAreaLabel(initShowArea), b -> {
            EnderInhibitorBlockEntity be = getClientBE();
            if (be == null) return;
            be.showArea = !be.showArea;
            b.setMessage(showAreaLabel(be.showArea));
        }).bounds(saX, saY + 3, SHOW_AREA_BUTTON_W, 14).build());

        int oy = dialog.getPaneAbsY(PANE_OFFSETS);
        int by = oy + 17;
        int bh = 12;

        // X offset buttons — 2px padding each side of the axis letter
        addRenderableWidget(Button.builder(Component.literal("−"),
                b -> sendOffset(0, -1)).bounds(leftPos + 30, by, 12, bh).build());
        addRenderableWidget(Button.builder(Component.literal("+"),
                b -> sendOffset(0, +1)).bounds(leftPos + 52, by, 12, bh).build());

        // Y offset buttons
        addRenderableWidget(Button.builder(Component.literal("−"),
                b -> sendOffset(1, -1)).bounds(leftPos + 86, by, 12, bh).build());
        addRenderableWidget(Button.builder(Component.literal("+"),
                b -> sendOffset(1, +1)).bounds(leftPos + 108, by, 12, bh).build());

        // Z offset buttons
        addRenderableWidget(Button.builder(Component.literal("−"),
                b -> sendOffset(2, -1)).bounds(leftPos + 142, by, 12, bh).build());
        addRenderableWidget(Button.builder(Component.literal("+"),
                b -> sendOffset(2, +1)).bounds(leftPos + 164, by, 12, bh).build());
    }

    private void sendOffset(int axis, int delta) {
        NetworkManager.sendToServer(new SetInhibitorOffsetPacket(menu.getPos(), axis, delta));
    }

    @Nullable
    private EnderInhibitorBlockEntity getClientBE() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var raw = level.getBlockEntity(menu.getPos());
        return raw instanceof EnderInhibitorBlockEntity be ? be : null;
    }

    private static Component showAreaLabel(boolean on) {
        return Component.literal(on ? "Hide Area" : "Show Area");
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
        // No player inventory label — this dialog has no item slots.
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
}
