package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI screen for the Mob Harvester.
 *
 * <p>Layout (176 px wide):
 * <ol>
 *   <li>Title bar (17 px)</li>
 *   <li>8 px blank gap</li>
 *   <li>{@link HarvesterUpgradeSlotsPane} — "Upgrades" header + 1×3 upgrade slots (36 px)</li>
 *   <li>8 px blank gap</li>
 *   <li>{@link PlayerInventoryPane} — 3×9 inventory + hotbar (80 px)</li>
 *   <li>5 px bottom border padding</li>
 * </ol>
 *
 * <p>Total height = 17 + 8 + 36 + 8 + 80 + 5 = 154 px.
 */
public class MobHarvesterScreen extends AbstractContainerScreen<MobHarvesterMenu> {

    private static final Identifier[] GHOST_UPGRADES = {
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/harvester_upgrade_sharpness_ghost.png"),
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/harvester_upgrade_looting_ghost.png"),
        Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/harvester_upgrade_beheading_ghost.png"),
    };

    private final Dialog dialog;

    public MobHarvesterScreen(MobHarvesterMenu menu, Inventory inv, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new HarvesterUpgradeSlotsPane(),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new PlayerInventoryPane(MobHarvesterMenu.SLOT_LEFT));
        super(menu, inv, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    // ── Init ─────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────────

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);

        for (int i = 0; i < MobHarvesterBlockEntity.UPGRADE_SLOTS; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) {
                g.blit(RenderPipelines.GUI_TEXTURED, GHOST_UPGRADES[i],
                        leftPos + MobHarvesterMenu.UPGRADE_X_START + i * 18,
                        topPos + MobHarvesterMenu.UPGRADE_TOP,
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
        g.text(font,
                Component.translatable("container.inventory"),
                MobHarvesterMenu.SLOT_LEFT,
                MobHarvesterMenu.INV_TOP - 10,
                0xFF404040,
                false);
    }

    // ── Input ─────────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }
}
