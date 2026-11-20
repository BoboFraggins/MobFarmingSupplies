package net.bobofraggins.mobfarmingsupplies.muffler;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorRegistry;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Muffler's block entity: the Ender Inhibitor's (area offset, settings menu, Show Area), plus a
 * place in {@link MufflerSounds} while it's loaded on a client, which is where sounds are muted.
 */
public class MufflerBlockEntity extends EnderInhibitorBlockEntity {

    public MufflerBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("muffler"), pos, state);
    }

    @Override
    protected InhibitorRegistry registry() {
        return InhibitorRegistry.MUFFLER;
    }

    /** How far its area reaches from the centre: a 7-block cube, the Absorption Hopper's pickup area. */
    public static final int RANGE = 3;

    @Override
    public int areaRange() {
        return RANGE;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide()) MufflerSounds.add(this);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && level.isClientSide()) MufflerSounds.add(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        MufflerSounds.remove(this);
    }
}
