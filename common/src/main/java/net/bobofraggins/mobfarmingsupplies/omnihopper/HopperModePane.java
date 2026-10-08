package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.SorterFilters;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.bobofraggins.mobfarmingsupplies.shared.ui.ToggleSwitch;

/**
 * The AND / OR toggle: whether an item must pass all of the hopper's filters, or just one. With no
 * Item Filter installed the hopper moves everything, so the toggle is hidden and "Everything" is
 * shown in its place.
 */
class HopperModePane implements IDialogPane {

    public static final int HEIGHT = 20;

    private static final int TOGGLE_W = ToggleSwitch.WIDTH;
    private static final int TOGGLE_H = ToggleSwitch.HEIGHT;
    private static final int TOGGLE_X = (PlayerInventoryPane.WIDTH - TOGGLE_W) / 2;
    private static final int TOGGLE_Y = (HEIGHT - TOGGLE_H) / 2;

    private final HopperConfigView config;
    private final Container filters;

    /** @param filters the menu's (synced) filter slots */
    HopperModePane(HopperConfigView config, Container filters) {
        this.config = config;
        this.filters = filters;
    }

    @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        if (!SorterFilters.hasAnyFilter(filters)) {
            Component everything = Component.translatable("gui.mobfarmingsupplies.omnidirectional_hopper.everything");
            g.text(font, everything, (PlayerInventoryPane.WIDTH - font.width(everything)) / 2,
                    (HEIGHT - font.lineHeight) / 2 + 1, 0xFF404040, false);
            return;
        }
        boolean and = config.andMode();
        ToggleSwitch.draw(g, font, TOGGLE_X, TOGGLE_Y, !and, Component.translatable(
                and ? "gui.mobfarmingsupplies.toggle.and" : "gui.mobfarmingsupplies.toggle.or"));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!SorterFilters.hasAnyFilter(filters)) return false;
        if (x < TOGGLE_X || x >= TOGGLE_X + TOGGLE_W || y < TOGGLE_Y || y >= TOGGLE_Y + TOGGLE_H) return false;
        config.toggleAndMode();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        return true;
    }
}
