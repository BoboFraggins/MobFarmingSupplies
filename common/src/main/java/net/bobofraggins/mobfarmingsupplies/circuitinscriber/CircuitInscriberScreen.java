package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** The Circuit Inscriber's screen; see {@link CircuitInscriberPane} for its working area. */
public class CircuitInscriberScreen extends AbstractContainerScreen<CircuitInscriberMenu> {

    private final Dialog dialog;

    public CircuitInscriberScreen(CircuitInscriberMenu menu, Inventory inventory, Component title) {
        Dialog d = new Dialog(
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new CircuitInscriberPane(menu),
                Dialog.blankPane(PlayerInventoryPane.WIDTH, 8),
                new PlayerInventoryPane(CircuitInscriberMenu.SLOT_LEFT));
        super(menu, inventory, title, d.totalWidth(), d.totalHeight());
        dialog = d;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        super.extractContents(g, mouseX, mouseY, partialTick);
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos - CircuitInscriberMenu.PANE_TOP;
        if (CircuitInscriberPane.overEnergyBar(localX, localY)) {
            g.setTooltipForNextFrame(Component.translatable("container.mobfarmingsupplies.circuit_inscriber.energy",
                    menu.energy(), menu.capacity()), mouseX, mouseY);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, Component.translatable("container.inventory"), CircuitInscriberMenu.SLOT_LEFT,
                CircuitInscriberMenu.INV_TOP - 10, 0xFF404040, false);
    }
}
