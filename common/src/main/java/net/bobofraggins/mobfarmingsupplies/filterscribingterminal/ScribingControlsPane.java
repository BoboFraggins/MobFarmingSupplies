package net.bobofraggins.mobfarmingsupplies.filterscribingterminal;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The centre controls of the Filter Scribing Terminal, stacked vertically and centred:
 * <ol>
 *   <li>A ghost item slot — clicking with an item on the cursor (or dropping one from JEI)
 *       shows a copy of it; nothing is taken from the player. Clicking with an empty cursor,
 *       or right-clicking, clears it.
 *   <li>An Is / Is Not toggle switch.
 *   <li>A scrollable list with at most one selected row.
 * </ol>
 *
 * <p>Changing the item repopulates the list (via {@code rowsForItem}) and selects its first
 * row; an empty slot empties the list. Every change is reported through {@code onChange} so
 * the screen can mirror the state to the server.
 */
public class ScribingControlsPane implements IDialogPane {

    public static final int WIDTH  = 176;
    public static final int HEIGHT = 96;

    // ── Ghost slot (18 × 18 frame, item drawn 1 px inside) ────────────────────────
    public static final int GHOST_X = (WIDTH - 18) / 2; // 79
    public static final int GHOST_Y = 4;

    // ── Toggle (64 × 16 sprites) ──────────────────────────────────────────────────
    private static final int TOGGLE_W = 64;
    private static final int TOGGLE_H = 16;
    private static final int TOGGLE_X = (WIDTH - TOGGLE_W) / 2; // 56
    private static final int TOGGLE_Y = GHOST_Y + 18 + 4;       // 26
    private static final Identifier TOGGLE_IS =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/toggle_is");
    private static final Identifier TOGGLE_IS_NOT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/toggle_is_not");

    // ── List ──────────────────────────────────────────────────────────────────────
    private static final int LIST_W       = 92;
    private static final int LIST_X       = (WIDTH - LIST_W) / 2; // 42
    private static final int LIST_Y       = TOGGLE_Y + TOGGLE_H + 4; // 46
    private static final int ROW_H        = 11;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_H       = VISIBLE_ROWS * ROW_H + 2; // 46, incl. 1 px frame
    private static final int SCROLLBAR_W  = 5;

    private static final int COLOR_FRAME_DARK  = 0xFF373737;
    private static final int COLOR_FRAME_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SLOT_BG     = 0xFF8B8B8B;
    private static final int COLOR_ROW_HOVER   = 0x40FFFFFF;
    private static final int COLOR_ROW_SEL     = 0xFF6B3FA0;
    private static final int COLOR_TEXT        = 0xFF404040;
    private static final int COLOR_TEXT_SEL    = 0xFFFFFFFF;
    private static final int COLOR_TRACK       = 0xFF6F6F6F;
    private static final int COLOR_THUMB       = 0xFFC6C6C6;

    private final Supplier<ItemStack> carried;
    private final Function<ItemStack, List<Component>> rowsForItem;
    private final Consumer<ScribingControlsPane> onChange;

    private ItemStack ghostItem = ItemStack.EMPTY;
    private boolean isNot = false;
    private List<Component> rows = List.of();
    private int selectedRow = -1;
    private int scrollOffset = 0;
    private boolean draggingThumb = false;

    /**
     * @param carried     the item currently held on the mouse cursor
     * @param rowsForItem the list rows to offer for a given matcher item
     * @param onChange    called after the item, toggle, or selection changes
     */
    public ScribingControlsPane(
            Supplier<ItemStack> carried, Function<ItemStack, List<Component>> rowsForItem,
            Consumer<ScribingControlsPane> onChange) {
        this.carried = carried;
        this.rowsForItem = rowsForItem;
        this.onChange = onChange;
    }

    // ── State ─────────────────────────────────────────────────────────────────────

    public ItemStack getGhostItem() { return ghostItem; }

    public void setGhostItem(ItemStack stack) {
        ghostItem = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        rows = List.copyOf(rowsForItem.apply(ghostItem));
        selectedRow = rows.isEmpty() ? -1 : 0;
        scrollOffset = 0;
        onChange.accept(this);
    }

    /** False = "Is", true = "Is Not". */
    public boolean isNot() { return isNot; }

    /** Index of the selected row, or -1 for none. */
    public int getSelectedRow() { return selectedRow; }

    /**
     * Shows a fixed example without an item in the ghost slot — used by the JEI recipe view,
     * which draws the matcher item itself. Scrolls so the selected row is visible. Doesn't
     * fire {@code onChange}.
     */
    public void showExample(List<Component> rows, boolean isNot, int selectedRow) {
        this.rows = List.copyOf(rows);
        this.isNot = isNot;
        this.selectedRow = selectedRow;
        this.scrollOffset = Mth.clamp(selectedRow - (VISIBLE_ROWS - 1), 0, maxScroll());
    }

    // ── IDialogPane ───────────────────────────────────────────────────────────────

    @Override public int preferredWidth()  { return WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        // Ghost slot
        drawInset(g, GHOST_X, GHOST_Y, 18, 18);
        if (!ghostItem.isEmpty()) g.item(ghostItem, GHOST_X + 1, GHOST_Y + 1);
        if (inGhost(mouseX, mouseY)) g.fill(GHOST_X + 1, GHOST_Y + 1, GHOST_X + 17, GHOST_Y + 17, 0x80FFFFFF);

        // Toggle
        g.blitSprite(RenderPipelines.GUI_TEXTURED, isNot ? TOGGLE_IS_NOT : TOGGLE_IS,
                TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H);

        // List
        drawInset(g, LIST_X, LIST_Y, LIST_W, LIST_H);
        int rowW = rowWidth();
        int hovered = rowAt(mouseX, mouseY);
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int index = scrollOffset + i;
            if (index >= rows.size()) break;
            int ry = LIST_Y + 1 + i * ROW_H;
            boolean selected = index == selectedRow;
            if (selected) g.fill(LIST_X + 1, ry, LIST_X + 1 + rowW, ry + ROW_H, COLOR_ROW_SEL);
            else if (index == hovered) g.fill(LIST_X + 1, ry, LIST_X + 1 + rowW, ry + ROW_H, COLOR_ROW_HOVER);
            String text = font.plainSubstrByWidth(rows.get(index).getString(), rowW - 4);
            g.text(font, Component.literal(text), LIST_X + 3, ry + 2, selected ? COLOR_TEXT_SEL : COLOR_TEXT, false);
        }

        if (maxScroll() > 0) {
            int trackX = LIST_X + LIST_W - 1 - SCROLLBAR_W;
            int trackY = LIST_Y + 1;
            int trackH = LIST_H - 2;
            g.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + trackH, COLOR_TRACK);
            int thumbY = thumbY();
            g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH(), COLOR_THUMB);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inGhost(x, y)) {
            ItemStack held = carried.get();
            setGhostItem(button == 1 ? ItemStack.EMPTY : held);
            return true;
        }
        if (x >= TOGGLE_X && x < TOGGLE_X + TOGGLE_W && y >= TOGGLE_Y && y < TOGGLE_Y + TOGGLE_H) {
            isNot = !isNot;
            playClick();
            onChange.accept(this);
            return true;
        }
        if (maxScroll() > 0 && inScrollbar(x, y)) {
            draggingThumb = true;
            scrollToMouse(y);
            return true;
        }
        int row = rowAt(x, y);
        if (row >= 0) {
            if (row != selectedRow) {
                playClick();
                selectedRow = row;
                onChange.accept(this);
            }
            return true;
        }
        return inList(x, y);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (!inList(x, y) || dy == 0) return false;
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(dy), 0, maxScroll());
        return true;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if (!draggingThumb) return false;
        scrollToMouse(y);
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean was = draggingThumb;
        draggingThumb = false;
        return was;
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────────

    /** Pane-local bounds of the ghost slot's item area, for JEI drop targets. */
    public static int ghostItemX() { return GHOST_X + 1; }
    public static int ghostItemY() { return GHOST_Y + 1; }

    private static boolean inGhost(double x, double y) {
        return x >= GHOST_X && x < GHOST_X + 18 && y >= GHOST_Y && y < GHOST_Y + 18;
    }

    private static boolean inList(double x, double y) {
        return x >= LIST_X && x < LIST_X + LIST_W && y >= LIST_Y && y < LIST_Y + LIST_H;
    }

    private boolean inScrollbar(double x, double y) {
        int trackX = LIST_X + LIST_W - 1 - SCROLLBAR_W;
        return x >= trackX && x < trackX + SCROLLBAR_W && y >= LIST_Y + 1 && y < LIST_Y + LIST_H - 1;
    }

    private int rowWidth() {
        return LIST_W - 2 - (maxScroll() > 0 ? SCROLLBAR_W : 0);
    }

    private int rowAt(double x, double y) {
        if (x < LIST_X + 1 || x >= LIST_X + 1 + rowWidth()) return -1;
        if (y < LIST_Y + 1 || y >= LIST_Y + 1 + VISIBLE_ROWS * ROW_H) return -1;
        int index = scrollOffset + (int) ((y - LIST_Y - 1) / ROW_H);
        return index < rows.size() ? index : -1;
    }

    private int maxScroll() {
        return Math.max(0, rows.size() - VISIBLE_ROWS);
    }

    private int thumbH() {
        int trackH = LIST_H - 2;
        return Math.max(6, trackH * VISIBLE_ROWS / Math.max(rows.size(), 1));
    }

    private int thumbY() {
        int trackH = LIST_H - 2;
        return LIST_Y + 1 + (trackH - thumbH()) * scrollOffset / Math.max(maxScroll(), 1);
    }

    private void scrollToMouse(double y) {
        int trackH = LIST_H - 2;
        double frac = (y - LIST_Y - 1 - thumbH() / 2.0) / Math.max(trackH - thumbH(), 1);
        scrollOffset = Mth.clamp((int) Math.round(frac * maxScroll()), 0, maxScroll());
    }

    private static void drawInset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w - 1, y + 1, COLOR_FRAME_DARK);            // top
        g.fill(x, y + 1, x + 1, y + h - 1, COLOR_FRAME_DARK);        // left
        g.fill(x + 1, y + h - 1, x + w, y + h, COLOR_FRAME_LIGHT);   // bottom
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, COLOR_FRAME_LIGHT); // right
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_SLOT_BG);   // interior
    }

    private static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
}
