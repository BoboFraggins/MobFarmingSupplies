package net.bobofraggins.mobfarmingsupplies.skulllights;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A String of Skull Lights hook. Of the pair, the one the string was started from (the origin)
 * holds the string: where its other end is and what it's made of. It draws the string, and while
 * the skulls glow it puts invisible light blocks in the air where they hang. The other end only
 * knows where the origin is.
 *
 * <p>While a string is being hung, its origin hook is up on its own: it knows who is holding the
 * string, which trails from it to their hand. It comes away (dropping nothing, as the string is
 * still in their inventory) if they cancel, wander too far, or stop carrying the string.
 *
 * <p>Taking either hook away (breaking it, its wall going, an explosion) removes the other one and
 * the light, and drops the string, pattern and all, where the hook was.
 */
public class SkullLightHookBlockEntity extends BlockEntity {

    /** How often the origin checks its light is still where the skulls are, in ticks. */
    private static final int LIGHT_REFRESH_TICKS = 200;
    private static final int LIGHT_LEVEL = LightBlock.MAX_LEVEL;
    /** How often a hook waiting for its other end checks on whoever is hanging it, in ticks. */
    private static final int HOLDER_CHECK_TICKS = 10;
    /** How far from its hook a string being hung can be carried before it slips off. */
    private static final double HOLDER_REACH = SkullLightsCurve.MAX_SPAN + 8;
    /** How long a hook waits for its holder to come back (to log in, say) before it comes away. */
    private static final int HOLDER_ABSENT_TICKS = 200;

    // Origin end
    @Nullable private BlockPos end;
    @Nullable private SkullLights lights;
    private final List<BlockPos> lit = new ArrayList<>();
    // Origin end, while being hung
    @Nullable private UUID holder;
    private int holderAbsentTicks;
    // Other end
    @Nullable private BlockPos origin;

    private int ticksToLightCheck; // 0 on load: check on the first tick
    private boolean removing;
    private boolean dropNothing;

    public SkullLightHookBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("skull_light_hook"), pos, state);
    }

    /** Makes this the origin of a string that {@code holder} is now carrying to its other end. */
    void startHanging(UUID holder, SkullLights lights) {
        this.holder = holder;
        this.lights = lights;
        changed();
    }

    /** Makes this the origin of a string to the hook at {@code end}. */
    void hang(BlockPos end, SkullLights lights) {
        this.holder = null;
        this.end = end;
        this.lights = lights;
        this.ticksToLightCheck = 0;
        changed();
    }

    /** Whether this hook is waiting for {@code player} to hang the other end of its string. */
    boolean awaits(Player player) {
        return end == null && player.getUUID().equals(holder);
    }

    /** Who is carrying this hook's string to its other end, if anyone. */
    @Nullable public UUID holder() { return holder; }

    /** Makes this the far end of the string held by the hook at {@code origin}. */
    void attachTo(BlockPos origin) {
        this.origin = origin;
        changed();
    }

    @Nullable public BlockPos end() { return end; }
    @Nullable public SkullLights lights() { return lights; }

    /** Taking this hook down drops nothing (a creative player broke it). */
    void dropNothing() {
        dropNothing = true;
    }

    /** Where the cord ties on to this hook. */
    public Vec3 attachPoint() {
        return attachPoint(getBlockPos(), getBlockState());
    }

    static Vec3 attachPoint(BlockPos pos, BlockState state) {
        return SkullLightsCurve.attachPoint(pos, state.getValue(SkullLightHookBlock.FACING));
    }

    // ── Light ─────────────────────────────────────────────────────────────────

    void serverTick(ServerLevel level) {
        if (holder != null) {
            if (level.getGameTime() % HOLDER_CHECK_TICKS == 0) checkHolder(level);
            return;
        }
        if (lights == null || end == null || --ticksToLightCheck > 0) return;
        BlockState endState = level.getBlockState(end);
        if (!endState.hasProperty(SkullLightHookBlock.FACING)) return; // not there yet: try next tick
        ticksToLightCheck = LIGHT_REFRESH_TICKS;
        Set<BlockPos> wanted = new HashSet<>();
        if (lights.lit()) {
            for (SkullLightsCurve.Hang hang : SkullLightsCurve.hangs(attachPoint(), attachPoint(end, endState))) {
                wanted.add(BlockPos.containing(hang.skullCentre()));
            }
        }
        boolean changed = lit.removeIf(pos -> {
            if (wanted.contains(pos) && level.getBlockState(pos).is(Blocks.LIGHT)) return false;
            removeLight(level, pos);
            return true;
        });
        for (BlockPos pos : wanted) {
            if (lit.contains(pos) || !level.isLoaded(pos) || !level.isEmptyBlock(pos)) continue;
            level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL), Block.UPDATE_ALL);
            lit.add(pos);
            changed = true;
        }
        if (changed) setChanged();
    }

    /** Lets the hook come away if whoever is hanging its string has gone, gone too far, or put it down. */
    private void checkHolder(ServerLevel level) {
        Player player = level.getPlayerByUUID(holder);
        if (player == null) {
            holderAbsentTicks += HOLDER_CHECK_TICKS;
            if (holderAbsentTicks >= HOLDER_ABSENT_TICKS) level.removeBlock(worldPosition, false);
            return;
        }
        holderAbsentTicks = 0;
        boolean tooFar = player.position().distanceTo(attachPoint()) > HOLDER_REACH;
        if (tooFar || !StringOfSkullLightsItem.isHanging(player, level, worldPosition)) {
            StringOfSkullLightsItem.forgetStart(player, level, worldPosition);
            if (tooFar) player.sendOverlayMessage(Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.slipped"));
            level.removeBlock(worldPosition, false);
        }
    }

    private static void removeLight(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) level.removeBlock(pos, false);
    }

    // ── Taking the string down ──────────────────────────────────────────────────

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (removing || level == null || level.isClientSide()) return;
        removing = true;
        if (lights != null) {
            takeDown(pos, dropNothing || end == null); // a string still being hung is in its holder's hands
        } else if (origin != null && level.getBlockEntity(origin) instanceof SkullLightHookBlockEntity originHook
                && pos.equals(originHook.end)) {
            originHook.removing = true;
            originHook.takeDown(pos, dropNothing);
            level.removeBlock(origin, false);
        }
    }

    /** Removes the light and the far hook, and drops the string at {@code dropAt} unless {@code dropNothing}. */
    private void takeDown(BlockPos dropAt, boolean dropNothing) {
        for (BlockPos pos : lit) removeLight(level, pos);
        lit.clear();
        if (!dropNothing && lights != null) {
            ItemStack string = new ItemStack(net.bobofraggins.mobfarmingsupplies.register.Registration.STRING_OF_SKULL_LIGHTS.get());
            string.set(net.bobofraggins.mobfarmingsupplies.register.Registration.SKULL_LIGHTS.get(), lights);
            Containers.dropItemStack(level, dropAt.getX() + 0.5, dropAt.getY() + 0.5, dropAt.getZ() + 0.5, string);
        }
        if (end != null && !end.equals(dropAt) && level.getBlockEntity(end) instanceof SkullLightHookBlockEntity endHook) {
            endHook.removing = true;
            level.removeBlock(end, false);
        }
        lights = null;
    }

    // ── Saving and syncing ────────────────────────────────────────────────────

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (end != null) output.store("End", BlockPos.CODEC, end);
        if (lights != null) output.store("Lights", SkullLights.CODEC, lights);
        if (!lit.isEmpty()) output.store("Lit", BlockPos.CODEC.listOf(), List.copyOf(lit));
        if (origin != null) output.store("Origin", BlockPos.CODEC, origin);
        if (holder != null) output.store("Holder", UUIDUtil.CODEC, holder);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        end = input.read("End", BlockPos.CODEC).orElse(null);
        lights = input.read("Lights", SkullLights.CODEC).orElse(null);
        lit.clear();
        input.read("Lit", BlockPos.CODEC.listOf()).ifPresent(lit::addAll);
        origin = input.read("Origin", BlockPos.CODEC).orElse(null);
        holder = input.read("Holder", UUIDUtil.CODEC).orElse(null);
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
