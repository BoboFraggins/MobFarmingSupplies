package net.bobofraggins.mobfarmingsupplies.shared.ui;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A dialog window that stacks {@link IDialogPane} instances vertically below a title bar.
 *
 * <p>Draws its own background using a 9-slice approach (four corners, four edges, body fill).
 * Corner textures are 5×5 px; edge textures are 1×5 or 5×1 and are tiled to fill the gap.
 * The body fill uses {@code fill()} with {@link #COLOR_BODY}.
 *
 * <p>Usage:
 * <pre>{@code
 * // In constructor (before super()):
 * dialog = new Dialog(paneA, paneB);
 * super(menu, inv, title, dialog.totalWidth(), dialog.totalHeight());
 *
 * // In init():
 * dialog.init(leftPos, topPos);
 *
 * // In extractContents():
 * dialog.render(graphics, font, title, mouseX, mouseY, partialTick);
 * }</pre>
 */
public class Dialog {

    private static final Identifier TEX_TL =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_tl.png");
    private static final Identifier TEX_TR =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_tr.png");
    private static final Identifier TEX_BL =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_bl.png");
    private static final Identifier TEX_BR =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_br.png");
    private static final Identifier TEX_TOP =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_top.png");
    private static final Identifier TEX_BOT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_bottom.png");
    private static final Identifier TEX_LEFT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_left.png");
    private static final Identifier TEX_RIGHT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_right.png");

    /** Height of the title bar area; panes are positioned below this. */
    public static final int TITLE_H = 17;

    /** Corner and edge thickness in pixels. */
    private static final int CORNER = 5;

    /**
     * Padding added below the last pane so content doesn't sit under the bottom border.
     */
    public static final int BOTTOM_PADDING = CORNER;

    private static final int COLOR_BODY = 0xFFC6C6C6;

    private final int width;
    private final List<IDialogPane> panes;
    private final int[] paneYOffsets;
    private final int bodyHeight;

    /** Which pane last consumed a mouseClicked (for drag routing). */
    private int activePaneIndex = -1;

    private int x;
    private int y;

    /**
     * Returns a no-op pane with the given dimensions and no rendered content.
     * Useful for spacing/padding within the Dialog.
     */
    public static IDialogPane blankPane(int width, int height) {
        return new IDialogPane() {
            @Override public int preferredWidth()  { return width; }
            @Override public int preferredHeight() { return height; }
            @Override public void render(GuiGraphicsExtractor g, Font f, int w, int lmx, int lmy, float pt) {}
        };
    }

    /**
     * Creates a dialog whose dimensions are derived from the supplied panes.
     * Width = max of all {@code pane.preferredWidth()}.
     * Height = {@link #TITLE_H} + sum of pane heights + {@link #BOTTOM_PADDING}.
     */
    public Dialog(IDialogPane... panes) {
        this.panes = List.of(panes);
        this.paneYOffsets = new int[panes.length];
        int totalH = 0;
        int maxW = 0;
        for (int i = 0; i < panes.length; i++) {
            paneYOffsets[i] = totalH;
            totalH += panes[i].preferredHeight();
            maxW = Math.max(maxW, panes[i].preferredWidth());
        }
        this.bodyHeight = totalH;
        this.width = maxW;
    }

    /** Call from {@code init()} with the screen's {@code leftPos} and {@code topPos}. */
    public void init(int screenX, int screenY) {
        this.x = screenX;
        this.y = screenY;
    }

    /** Total width of the dialog. */
    public int totalWidth() { return width; }

    /** Total height: title bar + pane heights + bottom padding. */
    public int totalHeight() { return TITLE_H + bodyHeight + BOTTOM_PADDING; }

    /** Absolute screen-space X of the pane at the given index (always {@code x}). */
    public int getPaneAbsX(int index) { return x; }

    /** Absolute screen-space Y of the pane at the given index. */
    public int getPaneAbsY(int index) { return y + TITLE_H + paneYOffsets[index]; }

    // ── Rendering ───────────────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor g, Font font, Component title, int mouseX, int mouseY, float pt) {
        int totalH = totalHeight();
        int bodyTop = y + CORNER;
        int bodyBot = y + totalH;
        int midW = width - 2 * CORNER;
        int midH = totalH - 2 * CORNER;

        // Top corners + top edge
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_TL, x, y, 0, 0, CORNER, CORNER, CORNER, CORNER);
        for (int px = 0; px < midW; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_TOP, x + CORNER + px, y, 0, 0, 1, CORNER, 1, CORNER);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_TR, x + width - CORNER, y, 0, 0, CORNER, CORNER, CORNER, CORNER);

        // Side edges + body fill
        for (int py = 0; py < midH; py++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_LEFT,  x,                 bodyTop + py, 0, 0, CORNER, 1, CORNER, 1);
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_RIGHT, x + width - CORNER, bodyTop + py, 0, 0, CORNER, 1, CORNER, 1);
        }
        g.fill(x + CORNER, bodyTop, x + width - CORNER, bodyBot - CORNER, COLOR_BODY);

        // Bottom edge + bottom corners
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_BL, x, bodyBot - CORNER, 0, 0, CORNER, CORNER, CORNER, CORNER);
        for (int px = 0; px < midW; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_BOT, x + CORNER + px, bodyBot - CORNER, 0, 0, 1, CORNER, 1, CORNER);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_BR, x + width - CORNER, bodyBot - CORNER, 0, 0, CORNER, CORNER, CORNER, CORNER);

        // Title left-aligned in title bar
        g.text(font, title, x + 8, y + 6, 0xFF404040, false);

        // Render panes
        for (int i = 0; i < panes.size(); i++) {
            int paneAbsY = getPaneAbsY(i);
            int lmx = mouseX - x;
            int lmy = mouseY - paneAbsY;
            g.pose().pushMatrix();
            g.pose().translate(x, paneAbsY);
            panes.get(i).render(g, font, width, lmx, lmy, pt);
            g.pose().popMatrix();
        }
    }

    // ── Input routing ────────────────────────────────────────────────────────────

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        activePaneIndex = -1;
        for (int i = 0; i < panes.size(); i++) {
            int paneAbsY = getPaneAbsY(i);
            int paneH = panes.get(i).preferredHeight();
            if (mouseY >= paneAbsY && mouseY < paneAbsY + paneH) {
                if (panes.get(i).mouseClicked(mouseX - x, mouseY - paneAbsY, button)) {
                    activePaneIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        for (int i = 0; i < panes.size(); i++) {
            int paneAbsY = getPaneAbsY(i);
            int paneH = panes.get(i).preferredHeight();
            if (mouseY >= paneAbsY && mouseY < paneAbsY + paneH) {
                return panes.get(i).mouseScrolled(mouseX - x, mouseY - paneAbsY, dx, dy);
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activePaneIndex >= 0) {
            int paneAbsY = getPaneAbsY(activePaneIndex);
            return panes.get(activePaneIndex).mouseDragged(mouseX - x, mouseY - paneAbsY, button, dragX, dragY);
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activePaneIndex >= 0) {
            int paneAbsY = getPaneAbsY(activePaneIndex);
            boolean result = panes.get(activePaneIndex).mouseReleased(mouseX - x, mouseY - paneAbsY, button);
            activePaneIndex = -1;
            return result;
        }
        return false;
    }
}
