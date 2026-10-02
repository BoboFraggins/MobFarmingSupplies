package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.network.SetVoidExcessPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.IDialogPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.ToggleSwitch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** The "Void Excess: On / Off" row — whether pickups that don't fit are destroyed. */
class VoidExcessPane implements IDialogPane {

    public static final int HEIGHT = 20;

    private static final int LABEL_GAP = 4;
    private static final int TOGGLE_Y = (HEIGHT - ToggleSwitch.HEIGHT) / 2;
    private static final int TEXT_COLOR = 0xFF404040;

    private static final Component LABEL = Component.translatable("gui.mobfarmingsupplies.absorption_hopper.void_excess");

    private final BlockPos pos;

    VoidExcessPane(BlockPos pos) {
        this.pos = pos;
    }

    @Override public int preferredWidth()  { return PlayerInventoryPane.WIDTH; }
    @Override public int preferredHeight() { return HEIGHT; }

    /** Label and switch are centred as a group, so the switch moves with the label's (translated) width. */
    private static int toggleX(Font font) {
        int groupW = font.width(LABEL) + LABEL_GAP + ToggleSwitch.WIDTH;
        return (PlayerInventoryPane.WIDTH - groupW) / 2 + font.width(LABEL) + LABEL_GAP;
    }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int width, int mouseX, int mouseY, float pt) {
        boolean on = isOn();
        int tx = toggleX(font);
        g.text(font, LABEL, tx - LABEL_GAP - font.width(LABEL), (HEIGHT - font.lineHeight) / 2 + 1, TEXT_COLOR, false);
        ToggleSwitch.draw(g, font, tx, TOGGLE_Y, on, Component.translatable(
                on ? "gui.mobfarmingsupplies.toggle.on" : "gui.mobfarmingsupplies.toggle.off"));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!ToggleSwitch.contains(x, y, toggleX(Minecraft.getInstance().font), TOGGLE_Y)) return false;
        AbsorptionHopperBlockEntity be = clientBE();
        if (be == null) return false;
        boolean on = !be.isVoidExcess();
        be.setVoidExcess(on); // show the change now; the server's block update confirms it
        NetworkManager.sendToServer(new SetVoidExcessPacket(pos, on));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        return true;
    }

    private boolean isOn() {
        AbsorptionHopperBlockEntity be = clientBE();
        return be != null && be.isVoidExcess();
    }

    private AbsorptionHopperBlockEntity clientBE() {
        var level = Minecraft.getInstance().level;
        return level != null && level.getBlockEntity(pos) instanceof AbsorptionHopperBlockEntity h ? h : null;
    }
}
