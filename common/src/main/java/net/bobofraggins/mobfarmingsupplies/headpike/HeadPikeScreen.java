package net.bobofraggins.mobfarmingsupplies.headpike;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** The Head Pike's screen: a dispenser's panel with one column of skull slots. */
public class HeadPikeScreen extends AbstractContainerScreen<HeadPikeMenu> {

    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/head_pike.png");
    /** The colour inside an empty slot, to cover the ghost skull behind a real one. */
    private static final int SLOT_COLOR = 0xFF8B8B8B;

    public HeadPikeScreen(HeadPikeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
        for (int i = 0; i < 3; i++) {
            if (menu.getSlot(i).hasItem()) {
                int x = leftPos + HeadPikeMenu.SKULL_X, y = topPos + HeadPikeMenu.SKULL_TOP + i * 18;
                graphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);
            }
        }
    }
}
