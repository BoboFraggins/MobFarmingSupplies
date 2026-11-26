package net.bobofraggins.mobfarmingsupplies.mobhead;

import net.bobofraggins.mobfarmingsupplies.shared.compat.ImplicitComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponentInput;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueInput;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** A placed Mob Head: which mob's head it is (null until set from the item). */
public class MobHeadBlockEntity extends BlockEntity implements ImplicitComponents {

    @Nullable private EntityType<?> mobType;

    public MobHeadBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("mob_head"), pos, state);
    }

    @Nullable
    public EntityType<?> getMobType() { return mobType; }

    // ── The mob type travels on the item (Registration is shadowed in BlockEntity subclasses) ──

    @Override
    public void applyImplicitComponents(DataComponentInput components) {
        mobType = components.get(net.bobofraggins.mobfarmingsupplies.register.Registration.MOB_HEAD_TYPE.get());
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        if (mobType != null) components.set(net.bobofraggins.mobfarmingsupplies.register.Registration.MOB_HEAD_TYPE.get(), mobType);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        if (mobType != null) output.store("MobType", BuiltInRegistries.ENTITY_TYPE.byNameCodec(), mobType);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        mobType = input.read("MobType", BuiltInRegistries.ENTITY_TYPE.byNameCodec()).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
