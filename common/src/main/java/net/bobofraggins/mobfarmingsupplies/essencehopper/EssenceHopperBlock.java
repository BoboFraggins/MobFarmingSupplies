package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlock;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** The Essence Hopper: an Absorption Hopper that keeps only a tank, for a fluid it collects. */
public class EssenceHopperBlock extends AbsorptionHopperBlock {

    public EssenceHopperBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssenceHopperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.ESSENCE_HOPPER_BE_TYPE.get(),
                AbsorptionHopperBlockEntity::serverTick);
    }
}
