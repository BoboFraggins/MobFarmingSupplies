package net.bobofraggins.mobfarmingsupplies.witherinhibitor;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Wither Inhibitor: the Ender Inhibitor's counterpart for Withers. Mounted the same way, with
 * the same area and menu, it keeps any Wither in its area from showing its boss bar, from exploding
 * when it's summoned, and from firing its explosive skulls (see {@link WitherInhibitorEvents}).
 */
public class WitherInhibitorBlock extends EnderInhibitorBlock {

    public static final MapCodec<WitherInhibitorBlock> CODEC = simpleCodec(WitherInhibitorBlock::new);

    public WitherInhibitorBlock(Properties props) {
        super(props);
    }

    @Override
    public MapCodec<WitherInhibitorBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitherInhibitorBlockEntity(pos, state);
    }

    /** Wisps of smoke round the prongs, instead of the Ender Inhibitor's portal particles. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double cx = pos.getX() + 0.5 + facing.getStepX() * 0.3;
        double cy = pos.getY() + 0.5 + facing.getStepY() * 0.3;
        double cz = pos.getZ() + 0.5 + facing.getStepZ() * 0.3;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    cx + (random.nextDouble() - 0.5) * 0.625, cy + (random.nextDouble() - 0.5) * 0.625,
                    cz + (random.nextDouble() - 0.5) * 0.625, 0, 0.02, 0);
        }
    }
}
