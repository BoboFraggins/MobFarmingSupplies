package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.network.SetPushSidesPacket;
import net.bobofraggins.mobfarmingsupplies.shared.ui.SideGridPane;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The Absorption Hopper's side grid: whether it pushes collected items and XP out of each side.
 * Mode 0 = not pushing, 1 = pushing. The push mask's bit layout is {@link IAbsorptionHopperBlockEntity#bitToWorldDir}.
 */
final class PushSidesModel implements SideGridPane.Model {

    private static final Identifier TEX_ON =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "widget/push_button_on");

    private final BlockPos pos;
    private int mask;

    PushSidesModel(BlockPos pos) {
        this.pos = pos;
        refresh();
    }

    @Override
    public void refresh() {
        var level = Minecraft.getInstance().level;
        mask = level != null && level.getBlockEntity(pos) instanceof IAbsorptionHopperBlockEntity h ? h.getPushSides() : 0;
    }

    @Override
    public int modeOf(Direction side) {
        return (mask & (1 << bitOf(side))) != 0 ? 1 : 0;
    }

    @Override
    public void cycle(Direction side) {
        mask ^= 1 << bitOf(side); // optimistic update — server will confirm
        NetworkManager.sendToServer(new SetPushSidesPacket(pos, mask));
    }

    @Override
    public SideGridPane.SideStyle style(int mode) {
        return mode == 1
                ? new SideGridPane.SideStyle(0, TEX_ON, Component.translatable("gui.mobfarmingsupplies.absorption_hopper.push.on"))
                : new SideGridPane.SideStyle(0, null, Component.translatable("gui.mobfarmingsupplies.absorption_hopper.push.off"));
    }

    private static int bitOf(Direction side) {
        for (int bit = 0; bit < 6; bit++) {
            if (IAbsorptionHopperBlockEntity.bitToWorldDir(bit) == side) return bit;
        }
        throw new IllegalArgumentException(side.toString());
    }
}
