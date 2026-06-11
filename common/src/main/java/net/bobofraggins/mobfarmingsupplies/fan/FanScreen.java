package net.bobofraggins.mobfarmingsupplies.fan;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.Nullable;

/**
 * GUI screen for the Fan.
 *
 * <p>Layout (176 px wide):
 * <ol>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px blank gap</li>
 *   <li>{@link FanUpgradeSlotsPane} — "Upgrades" header + 3 upgrade slots, centred row (36 px)</li>
 *   <li>4 px blank gap</li>
 *   <li>Show Area toggle button (20 px)</li>
 *   <li>12 px blank gap</li>
 *   <li>{@link PlayerInventoryPane} — 3×9 inventory + hotbar (80 px)</li>
 *   <li>5 px bottom border padding</li>
 * </ol>
 *
 * <p>Total height = 17 + 8 + 36 + 4 + 20 + 12 + 80 + 5 = 182 px.
 */
public class FanScreen extends AbstractContainerScreen<FanMenu> {

    private static final Identifier[] GHOST_UPGRADES = {
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/fan_upgrade_width_ghost.png"),
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/fan_upgrade_height_ghost.png"),
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/fan_upgrade_distance_ghost.png"),
    };

    private static final int PANE_GAP1      = 0;
    private static final int PANE_UPGRADES  = 1;
    private static final int PANE_GAP2      = 2;
    private static final int PANE_SHOW_AREA = 3;
    private static final int PANE_GAP3      = 4;
    private static final int PANE_PLAYER    = 5;

    private static final int SHOW_AREA_PANE_H   = 20;
    private static final int SHOW_AREA_BUTTON_W = 90;

    private final Dialog dialog;

    public FanScreen(FanMenu menu, Inventory inv, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new FanUpgradeSlotsPane(),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 4),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, SHOW_AREA_PANE_H),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 12),
                new PlayerInventoryPane(FanMenu.SLOT_LEFT));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    // ── Init ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);

        // Show Area toggle
        int saY = dialog.getPaneAbsY(PANE_SHOW_AREA);
        int saX = leftPos + (dialog.totalWidth() - SHOW_AREA_BUTTON_W) / 2;
        FanBlockEntity initBe = getClientBE();
        boolean initShowArea = initBe != null && initBe.showArea;
        addRenderableWidget(Button.builder(showAreaLabel(initShowArea), b -> {
            FanBlockEntity be = getClientBE();
            if (be == null) return;
            be.showArea = !be.showArea;
            b.setMessage(showAreaLabel(be.showArea));
        }).bounds(saX, saY + 3, SHOW_AREA_BUTTON_W, 14).build());
    }

    @Nullable
    private FanBlockEntity getClientBE() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var raw = level.getBlockEntity(menu.getPos());
        return raw instanceof FanBlockEntity f ? f : null;
    }

    private static Component showAreaLabel(boolean on) {
        return Component.literal(on ? "Hide Area" : "Show Area");
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        for (int i = 0; i < FanBlockEntity.UPGRADE_SLOTS; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) {
                g.blit(RenderPipelines.GUI_TEXTURED, GHOST_UPGRADES[i],
                        leftPos + FanMenu.UPGRADE_X_START + i * 18,
                        topPos + FanMenu.UPGRADE_TOP,
                        0, 0, 16, 16, 16, 16);
            }
        }

        super.extractContents(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Title is drawn by Dialog; render the "Inventory" label above player slots.
        g.text(font,
                Component.translatable("container.inventory"),
                FanMenu.SLOT_LEFT,
                FanMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }
}
