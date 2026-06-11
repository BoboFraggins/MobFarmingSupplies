package net.bobofraggins.mobfarmingsupplies.tank;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Tank's fill/drain UI.
 *
 * <p>Layout (176 × 173 px):
 * <pre>
 *   ┌─ Dialog title bar (17 px) ─────────┐
 *   │            [Input]          y = 20  │
 *   │              ↓                      │
 *   │            [Output]         y = 62  │
 *   ├──────────── settings (71 px) ───────┤  y = 88
 *   │  Player inventory (3 rows + hotbar) │
 *   └─────────────────────────────────────┘
 * </pre>
 */
public class TankScreen extends AbstractContainerScreen<TankMenu> {

    private static final Identifier GHOST_BUCKET  =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/bucket_ghost.png");
    private static final Identifier GHOST_BOTTLE  =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/glass_bottle_ghost.png");
    private static final Identifier GHOST_SYRINGE =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/experience_syringe_ghost.png");

    private static final int SETTINGS_PANE_H = 71;

    private final Dialog dialog;

    public TankScreen(TankMenu menu, Inventory inv, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, SETTINGS_PANE_H),
                new PlayerInventoryPane(TankMenu.INV_START_X));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        int x = leftPos;
        int y = topPos;

        // Draw the two transfer slot backgrounds
        drawSlot(g, x + TankMenu.FLUID_IN_X,  y + TankMenu.FLUID_IN_Y);
        drawSlot(g, x + TankMenu.FLUID_OUT_X, y + TankMenu.FLUID_OUT_Y);

        // Ghost-item hint in the input slot when empty — cycles bucket → bottle → syringe every 1.5 s
        if (menu.getSlot(0).getItem().isEmpty()) {
            long gameTime = this.minecraft != null && this.minecraft.level != null
                    ? this.minecraft.level.getGameTime() : 0L;
            int phase = (int) ((gameTime / 30) % 3); // 30 ticks ≈ 1.5 s
            Identifier ghostTex = switch (phase) {
                case 0  -> GHOST_BUCKET;
                case 1  -> GHOST_BOTTLE;
                default -> GHOST_SYRINGE;
            };
            g.blit(RenderPipelines.GUI_TEXTURED, ghostTex,
                    x + TankMenu.FLUID_IN_X, y + TankMenu.FLUID_IN_Y,
                    0, 0, 16, 16, 16, 16);
        }

        // Down-arrow between the two slots
        drawDownArrow(g, x + TankMenu.FLUID_IN_X, y + TankMenu.FLUID_IN_Y + 18);

        super.extractContents(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Title is drawn by Dialog; suppress the default title/inventory labels.
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────────

    private static void drawSlot(GuiGraphicsExtractor g, int sx, int sy) {
        g.fill(sx,      sy,      sx + 16, sy + 1,  0xFF373737); // top
        g.fill(sx,      sy + 1,  sx + 1,  sy + 16, 0xFF373737); // left
        g.fill(sx,      sy + 16, sx + 17, sy + 17, 0xFFFFFFFF); // bottom
        g.fill(sx + 16, sy,      sx + 17, sy + 16, 0xFFFFFFFF); // right
        g.fill(sx + 1,  sy + 1,  sx + 16, sy + 16, 0xFF8B8B8B); // interior
    }

    private static void drawDownArrow(GuiGraphicsExtractor g, int gapX, int gapY) {
        int cx  = gapX + 8;
        int top = gapY + 3;
        g.fill(cx - 1, top,      cx + 1, top + 9,  0xFF555555); // stem
        g.fill(cx - 4, top + 9,  cx + 4, top + 11, 0xFF555555); // arrowhead row 1
        g.fill(cx - 2, top + 11, cx + 2, top + 13, 0xFF555555); // arrowhead row 2
        g.fill(cx - 1, top + 13, cx + 1, top + 15, 0xFF555555); // arrowhead row 3
    }
}
