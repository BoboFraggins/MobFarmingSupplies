package net.bobofraggins.mobfarmingsupplies.glamping.present;

import net.minecraft.core.HolderLookup;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueInput;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;

public class PresentBlockEntity extends BlockEntity {

    public static final String TAG_WRAPPED_STATE = "wrapped_state";
    public static final String TAG_WRAPPED_ENTITY = "wrapped_entity";

    @Nullable
    private BlockState wrappedState;

    @Nullable
    private CompoundTag wrappedEntityData;

    public PresentBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("present"), pos, state);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        if (wrappedState != null) {
            output.store(TAG_WRAPPED_STATE, CompoundTag.CODEC, NbtUtils.writeBlockState(wrappedState));
        }
        if (wrappedEntityData != null) {
            output.store(TAG_WRAPPED_ENTITY, CompoundTag.CODEC, wrappedEntityData);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        wrappedEntityData = input.read(TAG_WRAPPED_ENTITY, CompoundTag.CODEC).orElse(null);
        wrappedState = input.read(TAG_WRAPPED_STATE, CompoundTag.CODEC)
                .map(saved -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), saved))
                .orElse(null);
    }
}
