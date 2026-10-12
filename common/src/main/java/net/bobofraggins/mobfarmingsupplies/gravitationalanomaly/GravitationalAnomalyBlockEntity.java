package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Gravitational Anomaly's block entity. For now it only animates things on the client: the
 * jets, and its particles.
 */
public class GravitationalAnomalyBlockEntity extends BlockEntity {

    /** Client: how fast the jets grow or die away, as a fraction of their length per tick. */
    private static final float JET_SPEED = 0.05f;

    /** Client: gas particles drawn in each tick, from this far out (blocks). */
    private static final int GAS_PER_TICK = 2;
    private static final double GAS_NEAR = 1.4, GAS_FAR = 2.6;
    /** Client: sparks shot out along each jet each tick, how fast (blocks per tick), and from where. */
    private static final int SPARKS_PER_TICK = 2;
    private static final double SPARK_SPEED = 0.45, SPARK_START = 0.3;

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
        be.spawnParticles(level, pos);
    }

    /** Gas drawn in from all around, and, while the jets are out, sparks shot along them. */
    private void spawnParticles(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        // Fully qualified: in a block entity, "Registration" is shadowed by an inherited interface.
        var gas = net.bobofraggins.mobfarmingsupplies.register.Registration.ACCRETION_PARTICLE.get();
        var spark = net.bobofraggins.mobfarmingsupplies.register.Registration.JET_PARTICLE.get();
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        for (int i = 0; i < GAS_PER_TICK; i++) {
            // A direction evenly over the sphere, and a distance.
            double up = random.nextDouble() * 2 - 1, around = random.nextDouble() * Mth.TWO_PI;
            double flat = Math.sqrt(1 - up * up), far = Mth.lerp(random.nextDouble(), GAS_NEAR, GAS_FAR);
            level.addParticle(gas, x, y, z,
                    far * flat * Math.cos(around), far * up, far * flat * Math.sin(around));
        }
        if (jets < 0.5f) return;
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < SPARKS_PER_TICK; i++) {
                double speed = SPARK_SPEED * (0.8 + 0.4 * random.nextDouble());
                level.addParticle(spark, x + random.nextGaussian() * 0.02,
                        y + side * SPARK_START, z + random.nextGaussian() * 0.02,
                        random.nextGaussian() * 0.01, side * speed, random.nextGaussian() * 0.01);
            }
        }
    }
}
