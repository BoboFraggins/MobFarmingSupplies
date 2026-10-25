package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Experience Pylon, in three columns split by vertical rules:
 * <pre>
 *   ┌─ Experience Pylon ───────────────────────────┐
 *   │            Stored XP: 12 Levels              │
 *   │    Store      │   [Input]    │   Retrieve     │
 *   │ [ 1 Level  ] │      ↓       │ [ 1 Level  ]   │
 *   │ [ 5 Levels ] │              │ [ 5 Levels ]   │
 *   │ [   All    ] │   [Output]   │ [   All    ]   │
 *   ├──────────────────────────────────────────────┤
 *   │  Player inventory                            │
 *   └──────────────────────────────────────────────┘
 * </pre>
 * The middle column works like the tank's: a bottle, bucket or syringe in the top slot is filled
 * or emptied into the bottom one.
 */
public class ExperiencePylonScreen extends AbstractContainerScreen<ExperiencePylonMenu> {

    private static final ResourceLocation GHOST_BUCKET  =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/bucket_ghost.png");
    private static final ResourceLocation GHOST_BOTTLE  =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/glass_bottle_ghost.png");
    private static final ResourceLocation GHOST_SYRINGE =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/experience_syringe_ghost.png");

    // Column bounds within the 176 px panel; the rules sit on the boundaries.
    private static final int LEFT_RULE_X  = 61;
    private static final int RIGHT_RULE_X = 114;
    // The panel's inside runs from x 4 to 172.
    private static final int LEFT_CENTRE  = (4 + LEFT_RULE_X) / 2;
    private static final int RIGHT_CENTRE = (RIGHT_RULE_X + 2 + 172) / 2;

    private static final int STORED_Y     = 21;
    private static final int LABEL_Y      = 33;
    private static final int BUTTON_W     = 48;
    private static final int BUTTON_H     = 16;
    private static final int FIRST_BUTTON_Y = 45;
    private static final int BUTTON_STEP  = 19;
    private static final int RULE_TOP     = 31;
    private static final int RULE_BOTTOM  = 17 + ExperiencePylonMenu.SETTINGS_PANE_H - 3;

    private static final int LABEL_COLOR = 0xFF404040;
    private static final int RULE_DARK   = 0xFF555555;
    private static final int RULE_LIGHT  = 0xFFFFFFFF;

    private final Dialog dialog;

    public ExperiencePylonScreen(ExperiencePylonMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, ExperiencePylonMenu.SETTINGS_PANE_H),
                new PlayerInventoryPane(ExperiencePylonMenu.INV_START_X));
        imageWidth = d.totalWidth();
        imageHeight = d.totalHeight();
        dialog = d;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
        addColumn(LEFT_CENTRE, ExperiencePylonMenu.STORE_1);
        addColumn(RIGHT_CENTRE, ExperiencePylonMenu.RETRIEVE_1);
    }

    /** The 1 Level, 5 Levels and All buttons, centred on {@code centreX}, sending ids from {@code firstId}. */
    private void addColumn(int centreX, int firstId) {
        String[] keys = {"one_level", "five_levels", "all"};
        for (int i = 0; i < keys.length; i++) {
            int id = firstId + i;
            addRenderableWidget(Button.builder(
                            Component.translatable("gui.mobfarmingsupplies.experience_pylon." + keys[i]),
                            button -> pressed(id))
                    .bounds(leftPos + centreX - BUTTON_W / 2, topPos + FIRST_BUTTON_Y + i * BUTTON_STEP,
                            BUTTON_W, BUTTON_H)
                    .build());
        }
    }

    private void pressed(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        int x = leftPos;
        int y = topPos;

        // The client's copy of the block entity is kept in sync, so it knows how much is stored.
        long stored = minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof ExperiencePylonBlockEntity pylon
                ? pylon.getAmount() : 0;
        drawCentred(g, PylonExperience.storedLine(stored), x + imageWidth / 2, y + STORED_Y);
        drawCentred(g, Component.translatable("gui.mobfarmingsupplies.experience_pylon.store"), x + LEFT_CENTRE, y + LABEL_Y);
        drawCentred(g, Component.translatable("gui.mobfarmingsupplies.experience_pylon.retrieve"), x + RIGHT_CENTRE, y + LABEL_Y);
        drawRule(g, x + LEFT_RULE_X, y);
        drawRule(g, x + RIGHT_RULE_X, y);

        PlayerInventoryPane.drawSlotBg(g, x + ExperiencePylonMenu.FLUID_IN_X, y + ExperiencePylonMenu.FLUID_IN_Y);
        PlayerInventoryPane.drawSlotBg(g, x + ExperiencePylonMenu.FLUID_OUT_X, y + ExperiencePylonMenu.FLUID_OUT_Y);

        // Ghost hint in the empty input slot: bucket, bottle, syringe in turn, 1.5 s each.
        if (menu.getSlot(0).getItem().isEmpty()) {
            long gameTime = minecraft != null && minecraft.level != null ? minecraft.level.getGameTime() : 0L;
            ResourceLocation ghost = switch ((int) ((gameTime / 30) % 3)) {
                case 0  -> GHOST_BUCKET;
                case 1  -> GHOST_BOTTLE;
                default -> GHOST_SYRINGE;
            };
            g.blit(ghost,
                    x + ExperiencePylonMenu.FLUID_IN_X, y + ExperiencePylonMenu.FLUID_IN_Y,
                    0, 0, 16, 16, 16, 16);
        }

        drawDownArrow(g, x + ExperiencePylonMenu.FLUID_IN_X, y + ExperiencePylonMenu.FLUID_IN_Y + 18);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // The Dialog draws the title; the column labels are drawn in extractContents.
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────────

    private void drawCentred(GuiGraphics g, Component text, int centreX, int textY) {
        g.drawString(font, text, centreX - font.width(text) / 2, textY, LABEL_COLOR, false);
    }

    /** An engraved vertical line: dark, with a light line beside it. */
    private static void drawRule(GuiGraphics g, int ruleX, int y) {
        g.fill(ruleX,     y + RULE_TOP, ruleX + 1, y + RULE_BOTTOM, RULE_DARK);
        g.fill(ruleX + 1, y + RULE_TOP, ruleX + 2, y + RULE_BOTTOM, RULE_LIGHT);
    }

    private static void drawDownArrow(GuiGraphics g, int gapX, int gapY) {
        int cx  = gapX + 8;
        int top = gapY + 3;
        g.fill(cx - 1, top,      cx + 1, top + 9,  0xFF555555);
        g.fill(cx - 4, top + 9,  cx + 4, top + 11, 0xFF555555);
        g.fill(cx - 2, top + 11, cx + 2, top + 13, 0xFF555555);
        g.fill(cx - 1, top + 13, cx + 1, top + 15, 0xFF555555);
    }
}
