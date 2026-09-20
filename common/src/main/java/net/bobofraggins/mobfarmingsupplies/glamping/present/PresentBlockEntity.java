package net.bobofraggins.mobfarmingsupplies.glamping.present;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class PresentBlockEntity extends BlockEntity {

    public static final String TAG_WRAPPED_STATE = "wrapped_state";
    public static final String TAG_WRAPPED_ENTITY = "wrapped_entity";

    @Nullable
    private BlockState wrappedState;

    @Nullable
    private CompoundTag wrappedEntityData;

    public PresentBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("present"), pos, state);
    }

    public boolean hasWrappedBlock() {
        return wrappedState != null;
    }

    @Nullable
    public BlockState getWrappedState() {
        return wrappedState;
    }

    @Nullable
    public CompoundTag getWrappedEntityData() {
        return wrappedEntityData;
    }

    public void setWrappedBlock(@Nullable BlockState state, @Nullable CompoundTag entityData) {
        this.wrappedState = state;
        this.wrappedEntityData = entityData;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (wrappedState != null) {
            output.store(TAG_WRAPPED_STATE, CompoundTag.CODEC, NbtUtils.writeBlockState(wrappedState));
        }
        if (wrappedEntityData != null) {
            output.store(TAG_WRAPPED_ENTITY, CompoundTag.CODEC, wrappedEntityData);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        wrappedEntityData = input.read(TAG_WRAPPED_ENTITY, CompoundTag.CODEC).orElse(null);
        wrappedState = input.read(TAG_WRAPPED_STATE, CompoundTag.CODEC)
                .map(tag -> NbtUtils.readBlockState(input.lookup().lookupOrThrow(Registries.BLOCK), tag))
                .orElse(null);
    }
}
