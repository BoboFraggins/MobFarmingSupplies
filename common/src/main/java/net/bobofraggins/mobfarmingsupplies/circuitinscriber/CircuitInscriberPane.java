package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * The Circuit Inscriber's working area: an energy bar on the left, the two input slots stacked,
 * a progress arrow, and the output slot.
 */
public class CircuitInscriberPane implements IDialogPane {

    public static final int HEIGHT = 54;

    private static final Identifier ARROW = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_X = 82;
    private static final int ARROW_Y = 18;
    private static final int BAR_X = 10;
    private static final int BAR_Y = 4;
    private static final int BAR_W = 10;
    private static final int BAR_H = 46;

    private final CircuitInscriberMenu menu;

    public CircuitInscriberPane(CircuitInscriberMenu menu) {
        this.menu = menu;
    }

    @Override
    public int preferredWidth() {
        return PlayerInventoryPane.WIDTH;
    }

    @Override
    public int preferredHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int localMouseX, int localMouseY,
                       float partialTick) {
        int top = CircuitInscriberMenu.PANE_TOP;
        PlayerInventoryPane.drawSlotBg(g, CircuitInscriberMenu.INPUT_X, CircuitInscriberMenu.FIRST_Y - top);
        PlayerInventoryPane.drawSlotBg(g, CircuitInscriberMenu.INPUT_X, CircuitInscriberMenu.SECOND_Y - top);
        PlayerInventoryPane.drawSlotBg(g, CircuitInscriberMenu.OUTPUT_X, CircuitInscriberMenu.OUTPUT_Y - top);

        // Progress: vanilla's furnace arrow, filled left to right.
        int filled = Math.round(24 * menu.progress());
        g.fill(ARROW_X, ARROW_Y + 6, ARROW_X + 22, ARROW_Y + 10, 0xFF8B8B8B);
        if (filled > 0) g.blitSprite(RenderPipelines.GUI_TEXTURED, ARROW, 24, 16, 0, 0, ARROW_X, ARROW_Y, filled, 16);

        // Energy: a sunken bar filled from the bottom.
        g.fill(BAR_X - 1, BAR_Y - 1, BAR_X + BAR_W + 1, BAR_Y + BAR_H + 1, 0xFF373737);
        g.fill(BAR_X, BAR_Y, BAR_X + BAR_W, BAR_Y + BAR_H, 0xFF1E1E1E);
        long capacity = menu.capacity();
        int level = capacity <= 0 ? 0 : Math.round(BAR_H * Math.min(1f, menu.energy() / (float) capacity));
        if (level > 0) g.fill(BAR_X, BAR_Y + BAR_H - level, BAR_X + BAR_W, BAR_Y + BAR_H, 0xFFD83A3A);
    }

    /** Whether the mouse (pane-local) is over the energy bar, for its tooltip. */
    public static boolean overEnergyBar(int localX, int localY) {
        return localX >= BAR_X && localX < BAR_X + BAR_W && localY >= BAR_Y && localY < BAR_Y + BAR_H;
    }
}
