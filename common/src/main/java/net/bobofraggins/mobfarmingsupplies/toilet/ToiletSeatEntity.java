package net.bobofraggins.mobfarmingsupplies.toilet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Invisible, zero-size entity a player or mob rides while sitting on a {@link ToiletBlock}.
 *
 * <p>Created when someone sits down and removed as soon as nobody is riding it or the toilet is
 * gone; losing its rider flushes and closes the lid. Seats are saved with their rider (a mob sat on
 * a toilet is still sitting there after a reload) - vanilla won't let anything ride an entity type
 * that can't be saved. The seat keeps no state of its own: its block position is the toilet, and
 * the way it faces comes from the toilet's blockstate. Riders are placed by {@link ToiletSeatHeights}.
 */
public class ToiletSeatEntity extends Entity {

    /** Height of the seat point (a seated rider's hips) above the toilet's base: just above the 5-pixel rim. */
    private static final double SEAT_HEIGHT = 0.35;

    /** Set while a player takes a mob's place, so the mob getting up doesn't flush or close the lid. */
    private boolean swappingRider;

    public ToiletSeatEntity(EntityType<? extends ToiletSeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    static ToiletSeatEntity create(Level level, BlockPos pos, Direction facing) {
        // Fully qualified: Entity inherits a nested type called Registration that shadows ours.
        ToiletSeatEntity seat = new ToiletSeatEntity(
                net.bobofraggins.mobfarmingsupplies.register.Registration.TOILET_SEAT.get(), level);
        seat.setPos(pos.getX() + 0.5, pos.getY() + SEAT_HEIGHT, pos.getZ() + 0.5);
        seat.setYRot(facing.toYRot()); // sit facing out of the bowl
        return seat;
    }

    /** The toilet this seat belongs to: the seat point is inside its block. */
    private BlockPos toiletPos() {
        return blockPosition();
    }

    /** The occupied seat on the toilet at {@code pos}, or {@code null} if nobody is sitting there. */
    @Nullable
    static ToiletSeatEntity findOccupied(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(ToiletSeatEntity.class, new AABB(pos), Entity::isVehicle)
                .stream().findFirst().orElse(null);
    }

    /** Whether someone is already sitting on the toilet at {@code pos}. */
    static boolean isOccupied(Level level, BlockPos pos) {
        return findOccupied(level, pos) != null;
    }

    /** Moves the current (mob) rider off and seats {@code player} instead; the lid stays up. */
    void swapRiderFor(Player player) {
        swappingRider = true;
        try {
            ejectPassengers();
        } finally {
            swappingRider = false;
        }
        player.startRiding(this, true, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        BlockState toilet = level().getBlockState(toiletPos());
        if (!isVehicle() || !(toilet.getBlock() instanceof ToiletBlock)) {
            discard();
        } else if (!toilet.getValue(ToiletBlock.OPEN)) {
            // After a reload the toilet's own "anyone still here?" check can run before this seat has
            // loaded and close the lid under a seated mob - put it back up.
            ToiletBlock.setOpen(level(), toiletPos(), true);
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof TamableAnimal pet) pet.setInSittingPose(true);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof TamableAnimal pet) pet.setInSittingPose(false);
        if (level().isClientSide() || swappingRider) return;
        // Flush on standing up - but not if the toilet itself has just been broken.
        BlockPos toiletPos = toiletPos();
        if (level().getBlockState(toiletPos).getBlock() instanceof ToiletBlock) {
            // Fully qualified: Entity inherits a nested type called Registration that shadows ours.
            level().playSound(null, toiletPos,
                    net.bobofraggins.mobfarmingsupplies.register.Registration.TOILET_FLUSH_SOUND.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        ToiletBlock.setOpen(level(), toiletPos, false);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!hasPassenger(passenger)) return;
        moveFunction.accept(passenger, getX(), getY() + ToiletSeatHeights.feetOffset(passenger, this), getZ());
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return Vec3.ZERO; // the seat entity's own position is the seat point
    }

    /** Keep a seated mob's head turned with its body rather than swivelling on its own. */
    @Override
    public void onPassengerTurned(Entity passenger) {
        passenger.setYHeadRot(passenger.getYRot());
    }

    /** Get up in front of the bowl rather than on top of the toilet. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockState toilet = level().getBlockState(toiletPos());
        if (!(toilet.getBlock() instanceof ToiletBlock)) return super.getDismountLocationForPassenger(passenger);
        BlockPos front = toiletPos().relative(toilet.getValue(ToiletBlock.FACING));
        if (level().getBlockState(front).getCollisionShape(level(), front).isEmpty()
                && level().getBlockState(front.above()).getCollisionShape(level(), front.above()).isEmpty()) {
            return Vec3.atBottomCenterOf(front);
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    // ── Nothing to sync, save or damage ─────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}
}
