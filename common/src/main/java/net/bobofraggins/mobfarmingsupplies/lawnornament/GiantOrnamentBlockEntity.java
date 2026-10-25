package net.bobofraggins.mobfarmingsupplies.lawnornament;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Where a giant lawn ornament's head is looking. Client only: every tick the head turns part of
 * the way towards the nearest player within {@value #WATCH_RANGE} blocks, and back to the front
 * when nobody is near.
 *
 * <p>A Giant Player Lawn Ornament also keeps its owner, whose skin it wears: saved, sent to
 * clients, and carried by the item as its {@code minecraft:profile} when broken.
 *
 * <p>Every giant ornament shares this block entity type, registered as
 * {@code giant_skeleton_lawn_ornament}: that id is already saved in worlds and in the village
 * yard template.
 */
public class GiantOrnamentBlockEntity extends BlockEntity {

    /** How near a player must be to be watched. */
    static final double WATCH_RANGE = 8.0;
    /** How far the head turns from the body at most, either way, in degrees. */
    private static final float MAX_HEAD_YAW = 75f;
    private static final float MAX_HEAD_PITCH = 40f;
    /** Share of the remaining turn made each tick. */
    private static final float TURN_PER_TICK = 0.25f;

    /** The NBT key of the owner, also written by {@link OrnamentSwapProcessor}. */
    static final String TAG_PROFILE = "profile";

    @Nullable private ResolvableProfile owner;

    private float headYaw, headPitch;
    private float prevHeadYaw, prevHeadPitch;

    public GiantOrnamentBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("giant_skeleton_lawn_ornament"), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GiantOrnamentBlockEntity be) {
        be.prevHeadYaw = be.headYaw;
        be.prevHeadPitch = be.headPitch;
        float targetYaw = 0f, targetPitch = 0f;
        // The figure is drawn larger than life, and its eyes with it.
        double eyeHeight = be.ornament().eyeHeight() * GiantOrnamentRenderer.SCALE;
        double cx = pos.getX() + 0.5, eyeY = pos.getY() + eyeHeight, cz = pos.getZ() + 0.5;
        Player player = level.getNearestPlayer(cx, eyeY, cz, WATCH_RANGE, EntitySelector.NO_SPECTATORS);
        if (player != null) {
            double dx = player.getX() - cx, dy = player.getEyeY() - eyeY, dz = player.getZ() - cz;
            float bodyYaw = state.getValue(GiantOrnamentBlock.FACING).toYRot();
            float worldYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            targetYaw = Mth.clamp(Mth.wrapDegrees(worldYaw - bodyYaw), -MAX_HEAD_YAW, MAX_HEAD_YAW);
            targetPitch = Mth.clamp((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))),
                    -MAX_HEAD_PITCH, MAX_HEAD_PITCH);
        }
        be.headYaw += Mth.wrapDegrees(targetYaw - be.headYaw) * TURN_PER_TICK;
        be.headPitch += (targetPitch - be.headPitch) * TURN_PER_TICK;
    }

    /** Head yaw relative to the body, in degrees, between ticks. */
    float headYaw(float partialTick) {
        return prevHeadYaw + Mth.wrapDegrees(headYaw - prevHeadYaw) * partialTick;
    }

    /** Head pitch in degrees (positive looks down), between ticks. */
    float headPitch(float partialTick) {
        return Mth.lerp(partialTick, prevHeadPitch, headPitch);
    }

    @Nullable
    public ResolvableProfile owner() {
        return owner;
    }

    /** Gives the ornament an owner, once (a Giant Player Lawn Ornament keeps its first). */
    void claim(ResolvableProfile profile) {
        if (owner != null) return;
        owner = profile;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null) output.store(TAG_PROFILE, ResolvableProfile.CODEC, owner);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        owner = input.read(TAG_PROFILE, ResolvableProfile.CODEC).orElse(null);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (owner != null) components.set(DataComponents.PROFILE, owner);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        ResolvableProfile profile = input.get(DataComponents.PROFILE);
        if (profile != null) owner = profile;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(TAG_PROFILE);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    GiantOrnament ornament() {
        return getBlockState().getBlock() instanceof GiantOrnamentBlock block ? block.ornament() : GiantOrnament.SKELETON;
    }

    Direction facing() {
        return getBlockState().getValue(GiantOrnamentBlock.FACING);
    }
}
