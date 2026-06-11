package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.network.SetPushSidesPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.architectury.networking.NetworkManager;

/**
 * Config-drawer pane for the Absorption Hopper's per-side push toggles.
 *
 * <p>Renders a 3×3 grid of 6 buttons arranged as:
 * <pre>
 *   .  U  .
 *   W  N  E
 *   .  D  S
 * </pre>
 * where U=Up, D=Down, W=West, N=North, E=East, S=South. Each button shows the block
 * adjacent on that face and toggles output for that side.
 *
 * <p>Side mask: bit 0=UP, 1=DOWN, 2=NORTH, 3=SOUTH, 4=EAST, 5=WEST.
 */
public class PushSidesPane implements IDialogPane {

    private static final int BIT_UP    = 0;
    private static final int BIT_DOWN  = 1;
    private static final int BIT_NORTH = 2;
    private static final int BIT_SOUTH = 3;
    private static final int BIT_EAST  = 4;
    private static final int BIT_WEST  = 5;

    private static final int BUTTON_SIZE   = 22;
    private static final int BUTTON_MARGIN = 1;
    private static final int GRID_W = 3 * BUTTON_SIZE + 2 * BUTTON_MARGIN; // 68
    private static final int LEFT_MARGIN = (PlayerInventoryPane.WIDTH - GRID_W) / 2; // 54
    private static final int HEADER_Y  = 5;
    private static final int BUTTONS_Y = 17;
    private static final int PANE_HEIGHT = BUTTONS_Y + GRID_W + 5; // 90

    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_DARK  = 0xFF555555;
    /** Matches {@link net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog#COLOR_BODY}. */
    private static final int COLOR_BODY  = 0xFFC6C6C6;

    private static final Identifier TEX_ON =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/push_button_on");

    /**
     * {col, row, bit} for each active cell in the 3×3 grid.
     * Layout:
     *   col: 0=left, 1=center, 2=right
     *   row: 0=top,  1=middle, 2=bottom
     */
    private static final int[][] BUTTONS = {
        {1, 0, BIT_UP},    // top-center
        {0, 1, BIT_WEST},  // middle-left
        {1, 1, BIT_SOUTH}, // middle-center
        {2, 1, BIT_EAST},  // middle-right
        {1, 2, BIT_DOWN},  // bottom-center
        {2, 2, BIT_NORTH}, // bottom-right
    };

    private final BlockPos pos;

    private int cachedSidesMask = 0;
    private final ItemStack[] cachedAdjacent = new ItemStack[6];
    private int refreshCounter = 0;

    public PushSidesPane(BlockPos pos) {
        this.pos = pos;
        java.util.Arrays.fill(cachedAdjacent, ItemStack.EMPTY);
    }

    private void refreshCache() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            cachedSidesMask = 0;
            java.util.Arrays.fill(cachedAdjacent, ItemStack.EMPTY);
            return;
        }
        var be = level.getBlockEntity(pos);
        cachedSidesMask = be instanceof IAbsorptionHopperBlockEntity h ? h.getPushSides() : 0;
        for (int[] btn : BUTTONS) {
            int bit = btn[2];
            Direction world = IAbsorptionHopperBlockEntity.bitToWorldDir(bit);
            var state = level.getBlockState(pos.relative(world));
            if (state.isAir()) {
                cachedAdjacent[bit] = ItemStack.EMPTY;
            } else {
                var item = state.getBlock().asItem();
                cachedAdjacent[bit] = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            }
        }
    }

    @Override
    public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }

    @Override
    public int preferredHeight() { return PANE_HEIGHT; }

    @Override
    public void render(
            GuiGraphicsExtractor g, Font font, int width, int localMouseX, int localMouseY, float partialTick) {
        if (refreshCounter++ % 4 == 0) refreshCache();

        g.text(font, Component.literal("Push Directions:"), 8, HEADER_Y, 0xFF404040, false);

        int sidesMask = cachedSidesMask;
        int stride = BUTTON_SIZE + BUTTON_MARGIN;

        for (int[] btn : BUTTONS) {
            int col = btn[0], row = btn[1], bit = btn[2];
            int bx = LEFT_MARGIN + col * stride;
            int by = BUTTONS_Y + row * stride;
            boolean on = (sidesMask & (1 << bit)) != 0;

            // Raised bevel
            g.fill(bx,                by,                bx + BUTTON_SIZE,     by + 1,               COLOR_LIGHT);
            g.fill(bx,                by + 1,            bx + 1,               by + BUTTON_SIZE,      COLOR_LIGHT);
            g.fill(bx,                by + BUTTON_SIZE,  bx + BUTTON_SIZE + 1, by + BUTTON_SIZE + 1,  COLOR_DARK);
            g.fill(bx + BUTTON_SIZE,  by,                bx + BUTTON_SIZE + 1, by + BUTTON_SIZE,      COLOR_DARK);

            if (on) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, TEX_ON,
                        bx + 1, by + 1, BUTTON_SIZE - 2, BUTTON_SIZE - 2);
            } else {
                g.fill(bx + 1, by + 1, bx + BUTTON_SIZE - 1, by + BUTTON_SIZE - 1, COLOR_BODY);
            }

            ItemStack adjacent = cachedAdjacent[bit];
            if (!adjacent.isEmpty()) {
                g.item(adjacent, bx + 3, by + 3);
            }
        }
    }

    @Override
    public boolean mouseClicked(double localX, double localY, int button) {
        if (button != 0) return false;
        int stride = BUTTON_SIZE + BUTTON_MARGIN;
        for (int[] btn : BUTTONS) {
            int col = btn[0], row = btn[1], bit = btn[2];
            int bx = LEFT_MARGIN + col * stride;
            int by = BUTTONS_Y + row * stride;
            if (localX >= bx && localX < bx + BUTTON_SIZE && localY >= by && localY < by + BUTTON_SIZE) {
                int updated = cachedSidesMask ^ (1 << bit);
                cachedSidesMask = updated; // optimistic update — server will confirm
                NetworkManager.sendToServer(new SetPushSidesPacket(pos, updated));
                return true;
            }
        }
        return false;
    }

}
