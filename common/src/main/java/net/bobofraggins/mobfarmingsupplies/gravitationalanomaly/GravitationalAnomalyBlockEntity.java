package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The Gravitational Anomaly's block entity. For now it only animates the jets on the client. */
public class GravitationalAnomalyBlockEntity extends BlockEntity {

    /** Client: how fast the jets grow or die away, as a fraction of their length per tick. */
    private static final float JET_SPEED = 0.05f;

    /** Client: how far out the jets reach, 0 (none) to 1 (full length). */
    public float jets;
    public float prevJets;

    public GravitationalAnomalyBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("gravitational_anomaly"), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GravitationalAnomalyBlockEntity be) {
        be.prevJets = be.jets;
        float target = state.getValue(GravitationalAnomalyBlock.ACTIVE) ? 1f : 0f;
        if (be.jets < target) be.jets = Math.min(target, be.jets + JET_SPEED);
        else if (be.jets > target) be.jets = Math.max(target, be.jets - JET_SPEED);
    }
}
