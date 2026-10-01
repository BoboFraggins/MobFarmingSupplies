package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import com.mojang.blaze3d.platform.InputConstants;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * The Logistic Sorter's side configuration — the same 3×3 layout as the Absorption Hopper's
 * {@link net.bobofraggins.mobfarmingsupplies.absorptionhopper.PushSidesPane}:
 * <pre>
 *   .  U  .
 *   W  S  E
 *   .  D  N
 * </pre>
 * Each button shows the adjacent block and is coloured by its {@link SideMode}; clicking cycles
 * unattached → Input (blue) → Matches (green) → Does Not Match (red) → unattached. Hovering a
 * button names its side and mode ({@link #hoverTooltip}, drawn by the screen).
 */
class SorterSidesPane implements IDialogPane {

    private static final int BUTTON_SIZE   = 22;
    private static final int BUTTON_MARGIN = 1;
    private static final int GRID_W = 3 * BUTTON_SIZE + 2 * BUTTON_MARGIN; // 68
    private static final int LEFT_MARGIN = (PlayerInventoryPane.WIDTH - GRID_W) / 2; // 54
    private static final int HEADER_Y  = 5;
    private static final int BUTTONS_Y = 17;
    public static final int HEIGHT = BUTTONS_Y + GRID_W + 5; // 90

    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_DARK  = 0xFF555555;
    private static final int COLOR_BODY  = 0xFFC6C6C6;

    /** {col, row} per direction, matching PushSidesPane. */
    private static final Map<Direction, int[]> BUTTONS = new EnumMap<>(Map.of(
            Direction.UP,    new int[]{1, 0},
            Direction.WEST,  new int[]{0, 1},
            Direction.SOUTH, new int[]{1, 1},
            Direction.EAST,  new int[]{2, 1},
            Direction.DOWN,  new int[]{1, 2},
            Direction.NORTH, new int[]{2, 2}));

    private final SorterConfigView config;
    private final Map<Direction, ItemStack> adjacent = new EnumMap<>(Direction.class);
    private int refreshCounter = 0;
    @Nullable private Component hoverTooltip;

    SorterSidesPane(SorterConfigView config) {
        this.config = config;
    }

    @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    private void refreshAdjacent() {
        var level = Minecraft.getInstance().level;
        for (Direction d : Direction.values()) {
            ItemStack icon = ItemStack.EMPTY;
            if (level != null) {
                var state = level.getBlockState(config.pos().relative(d));
                var item = state.getBlock().asItem();
                if (!state.isAir() && item != Items.AIR) icon = new ItemStack(item);
            }
            adjacent.put(d, icon);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        if (refreshCounter++ % 4 == 0) {
            config.refresh();
            refreshAdjacent();
        }
        g.text(font, Component.translatable("container.mobfarmingsupplies.logistic_sorter.connections"),
                8, HEADER_Y, 0xFF404040, false);

        hoverTooltip = null;
        for (var e : BUTTONS.entrySet()) {
            int bx = buttonX(e.getValue()), by = buttonY(e.getValue());
            SideMode mode = config.side(e.getKey());
            if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
                hoverTooltip = Component.translatable("container.mobfarmingsupplies.logistic_sorter.side_tooltip",
                        Component.translatable("container.mobfarmingsupplies.logistic_sorter.side." + e.getKey().getSerializedName()),
                        Component.translatable("container.mobfarmingsupplies.logistic_sorter.mode." + mode.getSerializedName()));
            }

            // Raised bevel
            g.fill(bx, by, bx + BUTTON_SIZE, by + 1, COLOR_LIGHT);
            g.fill(bx, by + 1, bx + 1, by + BUTTON_SIZE, COLOR_LIGHT);
            g.fill(bx, by + BUTTON_SIZE, bx + BUTTON_SIZE + 1, by + BUTTON_SIZE + 1, COLOR_DARK);
            g.fill(bx + BUTTON_SIZE, by, bx + BUTTON_SIZE + 1, by + BUTTON_SIZE, COLOR_DARK);
            g.fill(bx + 1, by + 1, bx + BUTTON_SIZE - 1, by + BUTTON_SIZE - 1,
                    mode == SideMode.NONE ? COLOR_BODY : mode.color());

            ItemStack icon = adjacent.getOrDefault(e.getKey(), ItemStack.EMPTY);
            if (!icon.isEmpty()) g.item(icon, bx + 3, by + 3);
        }
    }

    /** Tooltip for the side button under the mouse during the last render, or null. */
    @Nullable
    Component hoverTooltip() { return hoverTooltip; }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;
        for (var e : BUTTONS.entrySet()) {
            int bx = buttonX(e.getValue()), by = buttonY(e.getValue());
            if (x >= bx && x < bx + BUTTON_SIZE && y >= by && y < by + BUTTON_SIZE) {
                config.cycle(e.getKey());
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                return true;
            }
        }
        return false;
    }

    private static int buttonX(int[] cell) { return LEFT_MARGIN + cell[0] * (BUTTON_SIZE + BUTTON_MARGIN); }
    private static int buttonY(int[] cell) { return BUTTONS_Y + cell[1] * (BUTTON_SIZE + BUTTON_MARGIN); }
}
