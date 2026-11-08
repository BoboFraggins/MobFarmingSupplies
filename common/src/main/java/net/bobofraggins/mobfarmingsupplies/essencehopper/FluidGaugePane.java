package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Dialog pane with the Essence Hopper's upgrade slot and, right of it, its tank: a bar
 * filling left to right, marked in quarters. The slot's item is drawn by the screen.
 */
final class FluidGaugePane implements IDialogPane {

    /** As wide as the Absorption Hopper's window, which its offset buttons are spaced for. */
    static final int WIDTH = 196;
    private static final int HEIGHT = 18;
    /** Left of the upgrade slot, pane-local; {@link EssenceHopperMenu} puts the slot here. */
    static final int SLOT_X = 8;
    private static final int GAUGE_X = SLOT_X + 18 + 4;
    private static final int GAUGE_W = WIDTH - GAUGE_X - SLOT_X;
    private static final int GAUGE_H = 16;

    private static final Identifier GHOST =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "textures/item/blank_filter_ghost.png");

    private static final int COLOR_BORDER = 0xFF222222;
    private static final int COLOR_BG     = 0xFF4A4A4A;
    private static final int COLOR_TICK   = 0xFFDDDDDD;
    private static final int TICK_SHORT = 3, TICK_LONG = 5;

    private final BlockPos pos;
    private final EssenceHopperMenu menu;

    FluidGaugePane(EssenceHopperMenu menu) {
        this.menu = menu;
        this.pos = menu.getPos();
    }

    @Override public int preferredWidth()  { return WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        PlayerInventoryPane.drawSlotBg(g, SLOT_X, 0);
        if (menu.getSlot(EssenceHopperBlockEntity.UPGRADE_SLOT).getItem().isEmpty()) {
            g.blit(RenderPipelines.GUI_TEXTURED, GHOST, SLOT_X, 0, 0, 0, 16, 16, 16, 16);
        }
        g.fill(GAUGE_X - 1, -1, GAUGE_X + GAUGE_W + 1, GAUGE_H + 1, COLOR_BORDER);
        g.fill(GAUGE_X, 0, GAUGE_X + GAUGE_W, GAUGE_H, COLOR_BG);
        var level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof EssenceHopperBlockEntity be
                && be.getTankAmount() > 0) {
            int filled = (int) ((long) be.getTankAmount() * GAUGE_W / be.getTankCapacity());
            Essence essence = Essence.of(be.getTankFluid());
            if (essence != null) g.fill(GAUGE_X, 0, GAUGE_X + filled, GAUGE_H, essence.gaugeColor());
        }
        tick(g, GAUGE_X + GAUGE_W / 4, TICK_SHORT);
        tick(g, GAUGE_X + GAUGE_W / 2, TICK_LONG);
        tick(g, GAUGE_X + GAUGE_W * 3 / 4, TICK_SHORT);
    }

    private static void tick(GuiGraphicsExtractor g, int x, int len) {
        g.fill(x, 0, x + 1, len, COLOR_TICK);
        g.fill(x, GAUGE_H - len, x + 1, GAUGE_H, COLOR_TICK);
    }
}
