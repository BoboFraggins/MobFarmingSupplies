package net.bobofraggins.mobfarmingsupplies.shared.ui;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Animated slide-out config panel that appears to the left of the main dialog.
 *
 * <p>Contains {@link IDialogPane} instances stacked vertically. Toggled by a tab button that
 * protrudes from the dialog's left edge; slides open/closed with an ease-out cubic animation.
 *
 * <p>Call {@link #render} in {@code extractContents()} <em>before</em> the main dialog so the
 * drawer appears behind it. Call {@link #renderTab} <em>after</em> so the tab appears on top.
 */
public class ConfigDrawer {

    /** Width of the drawer body (does not include the tab). */
    public static final int WIDTH = 110;

    /** Width of the toggle-button tab that protrudes from the dialog's left edge. */
    public static final int TAB_W = 24;

    /** Height of the toggle-button tab. */
    public static final int TAB_H = 22;

    private static final long ANIM_MS = 200L;
    private static final int CORNER = 5;
    private static final int CONTENT_PAD = 5;
    private static final int VERT_PAD = 12;
    private static final int SIDE_PAD = 12;
    private static final int COLOR_BODY = 0xFFC6C6C6;

    private static final Identifier BUTTON_NORMAL =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/button_config");
    private static final Identifier BUTTON_HOVER =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/button_config_focused");

    private static final Identifier TEX_TL =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_tl.png");
    private static final Identifier TEX_BL =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_corner_bl.png");
    private static final Identifier TEX_TOP =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_top.png");
    private static final Identifier TEX_BOT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_bottom.png");
    private static final Identifier TEX_LEFT =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/dialog_edge_left.png");

    private final List<IDialogPane> panes;
    private final int[] paneYOffsets;
    private final int contentHeight;

    private boolean showTabButton = true;
    private boolean open = false;
    private float animFrom = 0f;
    private long animStartMs = -1L;

    private int dialogX;
    private int dialogY;
    private int dialogH;

    public ConfigDrawer(IDialogPane... panes) {
        this.panes = List.of(panes);
        this.paneYOffsets = new int[panes.length];
        int y = VERT_PAD;
        for (int i = 0; i < panes.length; i++) {
            paneYOffsets[i] = y;
            y += panes[i].preferredHeight();
            if (i < panes.length - 1) y += CONTENT_PAD;
        }
        this.contentHeight = y + VERT_PAD;
    }

    /** Call from {@code init()} with the dialog's screen-space origin and total height. */
    public void init(int dialogX, int dialogY, int dialogH) {
        this.dialogX = dialogX;
        this.dialogY = dialogY;
        this.dialogH = dialogH;
    }

    public void withoutTabButton() {
        showTabButton = false;
    }

    public boolean hasContent() {
        return !panes.isEmpty();
    }

    /** Toggles the drawer open or closed with animation. */
    public void toggle() {
        animFrom = getProgress(System.currentTimeMillis());
        open = !open;
        animStartMs = System.currentTimeMillis();
    }

    /** Renders the drawer body. Call before the main dialog. */
    public void render(GuiGraphicsExtractor g, Font font, int mouseX, int mouseY, float partialTick) {
        float p = getProgress(System.currentTimeMillis());
        if (p <= 0.001f) return;

        int drawerX = dialogX - TAB_W - WIDTH;
        int drawerTop = dialogY + 15;
        int drawerH = contentHeight + 2 * CORNER;
        int visibleLeft = dialogX - TAB_W - Math.round(WIDTH * p);

        g.enableScissor(visibleLeft, drawerTop, dialogX + 1, drawerTop + drawerH);
        g.fill(drawerX + CORNER, drawerTop + CORNER, dialogX + 1, drawerTop + drawerH - CORNER, COLOR_BODY);

        g.blit(RenderPipelines.GUI_TEXTURED, TEX_TL, drawerX, drawerTop, 0, 0, CORNER, CORNER, CORNER, CORNER);
        int edgeW = WIDTH + TAB_W - CORNER;
        for (int px = 0; px < edgeW; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_TOP, drawerX + CORNER + px, drawerTop, 0, 0, 1, CORNER, 1, CORNER);
        }

        int midH = drawerH - 2 * CORNER;
        for (int py = 0; py < midH; py++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_LEFT, drawerX, drawerTop + CORNER + py, 0, 0, CORNER, 1, CORNER, 1);
        }

        g.blit(RenderPipelines.GUI_TEXTURED, TEX_BL, drawerX, drawerTop + drawerH - CORNER, 0, 0, CORNER, CORNER, CORNER, CORNER);
        for (int px = 0; px < edgeW; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_BOT, drawerX + CORNER + px, drawerTop + drawerH - CORNER, 0, 0, 1, CORNER, 1, CORNER);
        }

        for (int i = 0; i < panes.size(); i++) {
            int paneAbsY = dialogY + 15 + paneYOffsets[i];
            int lmx = mouseX - drawerX - SIDE_PAD;
            int lmy = mouseY - paneAbsY;
            g.pose().pushMatrix();
            g.pose().translate(drawerX + SIDE_PAD, paneAbsY);
            panes.get(i).render(g, font, WIDTH, lmx, lmy, partialTick);
            g.pose().popMatrix();
        }

        g.disableScissor();
    }

    /** Renders the tab button. Call after the main dialog. */
    public void renderTab(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (panes.isEmpty()) return;
        float p = getProgress(System.currentTimeMillis());
        int tabX = dialogX - TAB_W + 5 - Math.round((WIDTH + TAB_W + 2) * p);
        int tabY = dialogY + 15;
        int midH = TAB_H - 2 * CORNER;

        g.fill(tabX + CORNER, tabY + CORNER, tabX + TAB_W + 1, tabY + TAB_H - CORNER, COLOR_BODY);
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_TL, tabX, tabY, 0, 0, CORNER, CORNER, CORNER, CORNER);
        for (int px = 0; px < TAB_W - CORNER + 1; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_TOP, tabX + CORNER + px, tabY, 0, 0, 1, CORNER, 1, CORNER);
        }
        for (int py = 0; py < midH; py++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_LEFT, tabX, tabY + CORNER + py, 0, 0, CORNER, 1, CORNER, 1);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_BL, tabX, tabY + TAB_H - CORNER, 0, 0, CORNER, CORNER, CORNER, CORNER);
        for (int px = 0; px < TAB_W - CORNER + 1; px++) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEX_BOT, tabX + CORNER + px, tabY + TAB_H - CORNER, 0, 0, 1, CORNER, 1, CORNER);
        }

        if (showTabButton) {
            boolean hovered = mouseX >= tabX && mouseX < tabX + TAB_W + 1 && mouseY >= tabY && mouseY < tabY + TAB_H;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, hovered ? BUTTON_HOVER : BUTTON_NORMAL, tabX + 4, tabY + 3, 16, 16);
        }
    }

    /** Routes a mouse click into the drawer (including tab toggle). Returns true if consumed. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (panes.isEmpty()) return false;
        if (showTabButton) {
            int tabX = dialogX - TAB_W + 5 - Math.round((WIDTH + TAB_W + 2) * getProgress(System.currentTimeMillis()));
            if (mouseX >= tabX && mouseX < tabX + TAB_W + 1
                    && mouseY >= dialogY + 15 && mouseY < dialogY + 15 + TAB_H) {
                toggle();
                return true;
            }
        }
        if (getProgress(System.currentTimeMillis()) < 0.99f) return false;

        int drawerX = dialogX - TAB_W - WIDTH;
        int drawerTop = dialogY + 15;
        int drawerH = contentHeight + 2 * CORNER;
        if (mouseX < drawerX || mouseX >= dialogX) return false;
        if (mouseY < drawerTop || mouseY >= drawerTop + drawerH) return false;

        for (int i = 0; i < panes.size(); i++) {
            int paneAbsY = dialogY + 15 + paneYOffsets[i];
            int paneH = panes.get(i).preferredHeight();
            if (mouseY >= paneAbsY && mouseY < paneAbsY + paneH) {
                return panes.get(i).mouseClicked(mouseX - drawerX - SIDE_PAD, mouseY - paneAbsY, button);
            }
        }
        return false;
    }

    private float getProgress(long now) {
        if (animStartMs < 0) return open ? 1f : 0f;
        float t = Math.min((now - animStartMs) / (float) ANIM_MS, 1f);
        float inv = 1f - t;
        float eased = 1f - inv * inv * inv;
        return open ? animFrom + (1f - animFrom) * eased : animFrom * (1f - eased);
    }
}
