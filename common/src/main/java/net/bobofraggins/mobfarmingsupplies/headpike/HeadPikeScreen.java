package net.bobofraggins.mobfarmingsupplies.headpike;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The Head Pike's screen: a dispenser's panel with one column of skull slots. */
public class HeadPikeScreen extends AbstractContainerScreen<HeadPikeMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/gui/head_pike.png");
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int i = 0; i < 3; i++) {
            if (menu.getSlot(i).hasItem()) {
                int x = leftPos + HeadPikeMenu.SKULL_X, y = topPos + HeadPikeMenu.SKULL_TOP + i * 18;
                graphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);
            }
        }
    }
}
