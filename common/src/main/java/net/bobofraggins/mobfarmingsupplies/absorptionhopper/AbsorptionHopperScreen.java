package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.network.SetHopperOffsetPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import dev.architectury.networking.NetworkManager;

import org.jetbrains.annotations.Nullable;

/**
 * Screen for the Absorption Hopper.
 *
 * <p>Layout (within the 176-wide dialog):
 * <ul>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px gap</li>
 *   <li>2 rows × 8 hopper slots + fluid gauge (36 px)</li>
 *   <li>8 px gap</li>
 *   <li>Offset controls row (22 px)</li>
 *   <li>6 px gap</li>
 *   <li>Push-sides (Exports) control (90 px)</li>
 *   <li>6 px gap</li>
 *   <li>Player inventory 3×9 + hotbar (80 px)</li>
 * </ul>
 */
public class AbsorptionHopperScreen extends AbstractContainerScreen<AbsorptionHopperMenu> {

    // ── Pane indices in the dialog ──────────────────────────────────────────────
    private static final int PANE_TOP_GAP   = 0;
    private static final int PANE_SLOTS     = 1;
    private static final int PANE_GAP1      = 2;
    private static final int PANE_OFFSETS   = 3;
    private static final int PANE_GAP2      = 4;
    private static final int PANE_SHOW_AREA = 5;
    private static final int PANE_GAP3      = 6;
    private static final int PANE_PUSH      = 7;
    private static final int PANE_GAP4      = 8;
    private static final int PANE_PLAYER    = 9;

    private static final int SHOW_AREA_PANE_H  = 20;
    private static final int SHOW_AREA_BUTTON_W = 90;

    // ── Gauge screen-relative positions (relative to topPos) ───────────────────
    private static final int GAUGE_SCREEN_Y    = Dialog.TITLE_H + 8; // 25
    private static final int GAUGE_COLOR_FILL  = 0xFF39FF14;
    private static final int GAUGE_COLOR_BORDER = 0xFF222222;

    private final Dialog dialog;

    private int cachedTankAmount    = 0;
    private int cachedTankCapacity  = AbsorptionHopperBlockEntity.TANK_CAPACITY;
    private int gaugeRefreshCounter = 0;

    public AbsorptionHopperScreen(AbsorptionHopperMenu menu, Inventory inv, Component title) {
        IDialogPane slotsPane   = new HopperSlotsPane();
        IDialogPane offsetsPane = new OffsetControlsPane();
        IDialogPane pushPane    = new PushSidesPane(menu.getPos());
        Dialog dialog_ = new Dialog(
                Dialog.blankPane(HopperSlotsPane.WIDTH, 8),           // PANE_TOP_GAP
                slotsPane,                                              // PANE_SLOTS
                Dialog.blankPane(HopperSlotsPane.WIDTH, 8),           // PANE_GAP1
                offsetsPane,                                            // PANE_OFFSETS
                Dialog.blankPane(HopperSlotsPane.WIDTH, 4),           // PANE_GAP2
                Dialog.blankPane(HopperSlotsPane.WIDTH, SHOW_AREA_PANE_H), // PANE_SHOW_AREA
                Dialog.blankPane(HopperSlotsPane.WIDTH, 4),           // PANE_GAP3
                pushPane,                                               // PANE_PUSH
                Dialog.blankPane(HopperSlotsPane.WIDTH, 6),           // PANE_GAP4
                new PlayerInventoryPane(AbsorptionHopperMenu.PLAYER_SLOT_LEFT)); // PANE_PLAYER
        super(menu, inv, title, dialog_.totalWidth(), dialog_.totalHeight());
        dialog = dialog_;
    }

    // ── Init ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);

        // Show Area toggle
        int saY = dialog.getPaneAbsY(PANE_SHOW_AREA);
        int saX = leftPos + (dialog.totalWidth() - SHOW_AREA_BUTTON_W) / 2;
        AbsorptionHopperBlockEntity initBe = getClientBE();
        boolean initShowArea = initBe != null && initBe.showArea;
        addRenderableWidget(Button.builder(showAreaLabel(initShowArea), b -> {
            AbsorptionHopperBlockEntity be = getClientBE();
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
        NetworkManager.sendToServer(new SetHopperOffsetPacket(menu.getPos(), axis, delta));
    }

    @Nullable
    private AbsorptionHopperBlockEntity getClientBE() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var raw = level.getBlockEntity(menu.getPos());
        return raw instanceof AbsorptionHopperBlockEntity h ? h : null;
    }

    private static Component showAreaLabel(boolean on) {
        return Component.literal(on ? "Hide Area" : "Show Area");
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        renderFluidGauge(g);
        super.extractContents(g, mouseX, mouseY, partialTick);
    }

    private static final int GAUGE_TICK_COLOR  = 0xFFDDDDDD;
    private static final int GAUGE_TICK_SHORT  = 3; // quarter marks
    private static final int GAUGE_TICK_LONG   = 5; // half mark

    private void renderFluidGauge(GuiGraphicsExtractor g) {
        if (gaugeRefreshCounter++ % 6 == 0) {
            AbsorptionHopperBlockEntity be = getClientBE();
            if (be != null) {
                cachedTankAmount   = be.getTankAmount();
                cachedTankCapacity = be.getTankCapacity();
            }
        }
        int tankAmount   = cachedTankAmount;
        int tankCapacity = cachedTankCapacity;

        int gaugeX = leftPos + HopperSlotsPane.GAUGE_X;
        int gaugeY = topPos  + GAUGE_SCREEN_Y;
        int gaugeW = HopperSlotsPane.GAUGE_W;
        int gaugeH = HopperSlotsPane.GAUGE_H;

        if (tankAmount > 0 && tankCapacity > 0) {
            int filledH = (int) ((long) tankAmount * gaugeH / tankCapacity);
            g.fill(gaugeX, gaugeY + gaugeH - filledH, gaugeX + gaugeW, gaugeY + gaugeH, GAUGE_COLOR_FILL);
        }

        // Tick marks drawn after fill so they're always visible.
        // Positions are measured from the top of the gauge (gauge fills bottom-up).
        gaugeTick(g, gaugeX, gaugeY + gaugeH / 4,     gaugeW, GAUGE_TICK_SHORT); // 3/4 full
        gaugeTick(g, gaugeX, gaugeY + gaugeH / 2,     gaugeW, GAUGE_TICK_LONG);  // 1/2 full
        gaugeTick(g, gaugeX, gaugeY + gaugeH * 3 / 4, gaugeW, GAUGE_TICK_SHORT); // 1/4 full
    }

    private void gaugeTick(GuiGraphicsExtractor g, int gaugeX, int y, int gaugeW, int len) {
        g.fill(gaugeX,              y, gaugeX + len,         y + 1, GAUGE_TICK_COLOR);
        g.fill(gaugeX + gaugeW - len, y, gaugeX + gaugeW, y + 1, GAUGE_TICK_COLOR);
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
                AbsorptionHopperMenu.PLAYER_SLOT_LEFT,
                AbsorptionHopperMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dialog.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (dialog.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (dialog.mouseReleased(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
    }
}
