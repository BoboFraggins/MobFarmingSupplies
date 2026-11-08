package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperMenu;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.OffsetControlsPane;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.PushSidesModel;
import net.bobofraggins.mobfarmingsupplies.network.SetHopperOffsetPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.SideGridPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

/** Screen for the Essence Hopper: its upgrade slot and tank, then the Absorption Hopper's zone and side controls. */
public class EssenceHopperScreen extends AbstractContainerScreen<EssenceHopperMenu> {

    private static final int PANE_OFFSETS   = 3;
    private static final int PANE_SHOW_AREA = 5;

    private static final int SHOW_AREA_PANE_H   = 20;
    private static final int SHOW_AREA_BUTTON_W = 90;

    private final Dialog dialog;
    private final SideGridPane pushPane;

    public EssenceHopperScreen(EssenceHopperMenu menu, Inventory inv, Component title) {
        SideGridPane pushPane = new SideGridPane(menu.getPos(),
                Component.translatable("gui.mobfarmingsupplies.absorption_hopper.push_directions"),
                new PushSidesModel(menu.getPos()));
        Dialog dialog_ = new Dialog(
                Dialog.blankPane(FluidGaugePane.WIDTH, 8),
                new FluidGaugePane(menu),
                Dialog.blankPane(FluidGaugePane.WIDTH, 8),
                new OffsetControlsPane(),                               // PANE_OFFSETS
                Dialog.blankPane(FluidGaugePane.WIDTH, 4),
                Dialog.blankPane(FluidGaugePane.WIDTH, SHOW_AREA_PANE_H), // PANE_SHOW_AREA
                Dialog.blankPane(FluidGaugePane.WIDTH, 4),
                pushPane,
                Dialog.blankPane(FluidGaugePane.WIDTH, 6),
                new PlayerInventoryPane(AbsorptionHopperMenu.PLAYER_SLOT_LEFT));
        super(menu, inv, title, dialog_.totalWidth(), dialog_.totalHeight());
        dialog = dialog_;
        this.pushPane = pushPane;
    }

    @Override
    protected void init() {
        super.init();
        dialog.init(leftPos, topPos);

        int saX = leftPos + (dialog.totalWidth() - SHOW_AREA_BUTTON_W) / 2;
        EssenceHopperBlockEntity initBe = clientBE();
        addRenderableWidget(Button.builder(showAreaLabel(initBe != null && initBe.showArea), b -> {
            EssenceHopperBlockEntity be = clientBE();
            if (be == null) return;
            be.showArea = !be.showArea;
            b.setMessage(showAreaLabel(be.showArea));
        }).bounds(saX, dialog.getPaneAbsY(PANE_SHOW_AREA) + 3, SHOW_AREA_BUTTON_W, 14).build());

        int by = dialog.getPaneAbsY(PANE_OFFSETS) + 17;
        for (int axis = 0; axis < 3; axis++) {
            int x = leftPos + 30 + axis * 56;
            int a = axis;
            addRenderableWidget(Button.builder(Component.literal("−"), b -> sendOffset(a, -1)).bounds(x, by, 12, 12).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> sendOffset(a, +1)).bounds(x + 22, by, 12, 12).build());
        }
    }

    private void sendOffset(int axis, int delta) {
        NetworkManager.sendToServer(new SetHopperOffsetPacket(menu.getPos(), axis, delta));
    }

    @Nullable
    private EssenceHopperBlockEntity clientBE() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        return level.getBlockEntity(menu.getPos()) instanceof EssenceHopperBlockEntity h ? h : null;
    }

    private static Component showAreaLabel(boolean on) {
        return Component.literal(on ? "Hide Area" : "Show Area");
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        dialog.render(g, font, title, mouseX, mouseY, partialTick);
        super.extractContents(g, mouseX, mouseY, partialTick);
        Component tooltip = pushPane.hoverTooltip();
        if (tooltip != null && menu.getCarried().isEmpty()) g.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractBackground(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // The Dialog draws the title.
        g.text(font, Component.translatable("container.inventory"), AbsorptionHopperMenu.PLAYER_SLOT_LEFT,
                EssenceHopperMenu.INV_TOP - 10, 0xFF404040, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dialog.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (dialog.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (dialog.mouseReleased(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (dialog.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, consumed);
    }
}
