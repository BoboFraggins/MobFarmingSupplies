package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.bobofraggins.mobfarmingsupplies.shared.ui.ToggleSwitch;

/** The AND / OR toggle: whether an item must pass all of the sorter's filters, or just one. */
class SorterModePane implements IDialogPane {

    public static final int HEIGHT = 20;

    private static final int TOGGLE_W = ToggleSwitch.WIDTH;
    private static final int TOGGLE_H = ToggleSwitch.HEIGHT;
    private static final int TOGGLE_X = (PlayerInventoryPane.WIDTH - TOGGLE_W) / 2;
    private static final int TOGGLE_Y = (HEIGHT - TOGGLE_H) / 2;

    private final SorterConfigView config;

    SorterModePane(SorterConfigView config) {
        this.config = config;
    }

    @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        boolean and = config.andMode();
        ToggleSwitch.draw(g, font, TOGGLE_X, TOGGLE_Y, !and, Component.translatable(
                and ? "gui.mobfarmingsupplies.toggle.and" : "gui.mobfarmingsupplies.toggle.or"));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (x < TOGGLE_X || x >= TOGGLE_X + TOGGLE_W || y < TOGGLE_Y || y >= TOGGLE_Y + TOGGLE_H) return false;
        config.toggleAndMode();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        return true;
    }
}
