package net.bobofraggins.mobfarmingsupplies.mobhead;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/** A placed Mob Head: which mob's head it is (null until set from the item). */
public class MobHeadBlockEntity extends BlockEntity {

    @Nullable private EntityType<?> mobType;

    public MobHeadBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("mob_head"), pos, state);
    }

    @Nullable
    public EntityType<?> getMobType() { return mobType; }

    // ── The mob type travels on the item (Registration is shadowed in BlockEntity subclasses) ──

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        mobType = components.get(net.bobofraggins.mobfarmingsupplies.register.Registration.MOB_HEAD_TYPE.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (mobType != null) components.set(net.bobofraggins.mobfarmingsupplies.register.Registration.MOB_HEAD_TYPE.get(), mobType);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (mobType != null) output.store("MobType", BuiltInRegistries.ENTITY_TYPE.byNameCodec(), mobType);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mobType = input.read("MobType", BuiltInRegistries.ENTITY_TYPE.byNameCodec()).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
