package net.bobofraggins.mobfarmingsupplies.omnihopper;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.shared.ui.SideGridPane;
import net.bobofraggins.mobfarmingsupplies.network.SetOmniHopperConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * Client-side view of an Omnidirectional Hopper's side modes and AND/OR mode, shared by the screen's
 * panes. Changes apply immediately (optimistically) and are sent to the server; otherwise the
 * view follows the synced client block entity.
 */
final class HopperConfigView implements SideGridPane.Model {

    /** How long a local change wins over (possibly stale) block entity data. */
    private static final long LOCAL_GRACE_MS = 500;

    private final BlockPos pos;
    private int sides;
    private boolean andMode = true;
    private long lastLocalChange;

    HopperConfigView(BlockPos pos) {
        this.pos = pos;
        refresh();
    }

    BlockPos pos() { return pos; }

    @Override
    public void refresh() {
        if (System.currentTimeMillis() - lastLocalChange < LOCAL_GRACE_MS) return;
        var level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(pos) instanceof HopperConfigurable be) {
            sides = be.packedSides();
            andMode = be.isAndMode();
        }
    }

    HopperSide side(Direction d) { return OmniHopperBlockEntity.unpackSide(sides, d); }

    @Override
    public int modeOf(Direction side) { return side(side).ordinal(); }

    @Override
    public SideGridPane.SideStyle style(int mode) {
        HopperSide m = HopperSide.byOrdinal(mode);
        return new SideGridPane.SideStyle(m == HopperSide.NONE ? 0 : m.color(), null,
                Component.translatable("container.mobfarmingsupplies.omnidirectional_hopper.mode." + m.getSerializedName()));
    }

    boolean andMode() { return andMode; }

    @Override
    public void cycle(Direction d) {
        int shift = 2 * d.get3DDataValue();
        sides = (sides & ~(3 << shift)) | (side(d).next().ordinal() << shift);
        send();
    }

    void toggleAndMode() {
        andMode = !andMode;
        send();
    }

    private void send() {
        lastLocalChange = System.currentTimeMillis();
        NetworkManager.sendToServer(new SetOmniHopperConfigPacket(pos, sides, andMode));
    }
}
