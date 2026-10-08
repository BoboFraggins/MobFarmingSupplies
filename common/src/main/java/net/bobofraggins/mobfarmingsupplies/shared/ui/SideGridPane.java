package net.bobofraggins.mobfarmingsupplies.shared.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideLayout;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideOriented;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * The side-configuration grid shared by the Absorption Hopper, Logistic Sorter, Omnidirectional
 * Hopper and Einstein-Rosen Bridge. Six buttons, laid out as described in {@link SideLayout} (so they
 * follow how the block was placed); each shows the neighbouring block and is drawn in the style of its
 * side's current mode. Clicking cycles the mode; hovering names the side and mode
 * ({@link #hoverTooltip}, drawn by the screen). The modes themselves come from the block's
 * {@link Model}.
 */
public class SideGridPane implements IDialogPane {

    /** How a mode looks: a sprite if given, otherwise a fill colour (0 = plain, like the dialog body). */
    public record SideStyle(int fillColor, @Nullable Identifier sprite, Component name) {}

    /** A block's side modes, as the client sees them. Modes are numbered from 0. */
    public interface Model {
        /** Re-reads the block's current settings (called a few times a second). */
        void refresh();

        int modeOf(Direction side);

        /** Advances {@code side} to its next mode and tells the server. */
        void cycle(Direction side);

        SideStyle style(int mode);
    }

    private static final int BUTTON_SIZE   = 22;
    private static final int BUTTON_MARGIN = 1;
    private static final int GRID_W = 3 * BUTTON_SIZE + 2 * BUTTON_MARGIN; // 68
    private static final int LEFT_MARGIN = (PlayerInventoryPane.WIDTH - GRID_W) / 2; // 54
    private static final int HEADER_Y  = 5;
    private static final int BUTTONS_Y = 17;
    public static final int HEIGHT = BUTTONS_Y + GRID_W + 5; // 90

    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_DARK  = 0xFF555555;
    /** Matches {@link Dialog#COLOR_BODY}. */
    private static final int COLOR_BODY  = 0xFFC6C6C6;

    private final BlockPos pos;
    private final Component header;
    private final Model model;

    private Direction[] layout;
    private final ItemStack[] adjacent = new ItemStack[SideLayout.CELLS];
    private int refreshCounter = 0;
    @Nullable private Component hoverTooltip;

    public SideGridPane(BlockPos pos, Component header, Model model) {
        this.pos = pos;
        this.header = header;
        this.model = model;
        this.layout = SideLayout.LEGACY;
        java.util.Arrays.fill(adjacent, ItemStack.EMPTY);
    }

    @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    private void refresh() {
        model.refresh();
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        layout = SideLayout.ofNullable(level.getBlockEntity(pos) instanceof SideOriented o ? o.getSideOrientation() : null);
        for (int cell = 0; cell < SideLayout.CELLS; cell++) {
            var state = level.getBlockState(pos.relative(layout[cell]));
            var item = state.getBlock().asItem();
            adjacent[cell] = state.isAir() || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        if (refreshCounter++ % 4 == 0) refresh();
        g.text(font, header, 8, HEADER_Y, 0xFF404040, false);

        hoverTooltip = null;
        for (int cell = 0; cell < SideLayout.CELLS; cell++) {
            int bx = buttonX(cell), by = buttonY(cell);
            Direction side = layout[cell];
            SideStyle style = model.style(model.modeOf(side));
            if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
                hoverTooltip = Component.translatable("gui.mobfarmingsupplies.side_grid.tooltip",
                        Component.translatable("gui.mobfarmingsupplies.side_grid." + SideLayout.CELL_NAMES[cell]),
                        Component.translatable("gui.mobfarmingsupplies.side_grid.direction." + side.getSerializedName()),
                        style.name());
            }

            // Raised bevel
            g.fill(bx, by, bx + BUTTON_SIZE, by + 1, COLOR_LIGHT);
            g.fill(bx, by + 1, bx + 1, by + BUTTON_SIZE, COLOR_LIGHT);
            g.fill(bx, by + BUTTON_SIZE, bx + BUTTON_SIZE + 1, by + BUTTON_SIZE + 1, COLOR_DARK);
            g.fill(bx + BUTTON_SIZE, by, bx + BUTTON_SIZE + 1, by + BUTTON_SIZE, COLOR_DARK);
            if (style.sprite() != null) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, style.sprite(), bx + 1, by + 1, BUTTON_SIZE - 2, BUTTON_SIZE - 2);
            } else {
                g.fill(bx + 1, by + 1, bx + BUTTON_SIZE - 1, by + BUTTON_SIZE - 1,
                        style.fillColor() == 0 ? COLOR_BODY : style.fillColor());
            }

            if (!adjacent[cell].isEmpty()) g.item(adjacent[cell], bx + 3, by + 3);
        }
    }

    /** Tooltip for the button under the mouse during the last render, or null. */
    @Nullable
    public Component hoverTooltip() { return hoverTooltip; }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;
        for (int cell = 0; cell < SideLayout.CELLS; cell++) {
            int bx = buttonX(cell), by = buttonY(cell);
            if (x >= bx && x < bx + BUTTON_SIZE && y >= by && y < by + BUTTON_SIZE) {
                model.cycle(layout[cell]);
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                return true;
            }
        }
        return false;
    }

    private static int buttonX(int cell) { return LEFT_MARGIN + SideLayout.CELL_POS[cell][0] * (BUTTON_SIZE + BUTTON_MARGIN); }
    private static int buttonY(int cell) { return BUTTONS_Y + SideLayout.CELL_POS[cell][1] * (BUTTON_SIZE + BUTTON_MARGIN); }
}
