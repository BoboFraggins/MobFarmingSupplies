package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Turns heat from the block below into energy (FE on NeoForge, Team Reborn Energy on Fabric):
 * 50 per tick over lava, 25 over magma and 15 over a lit campfire, times the tier's multiplier.
 * Energy collects in a buffer and is pushed into every neighbour that accepts it; neighbours can
 * also pull it (see {@link StirlingEnginePlatform}).
 */
public class StirlingEngineBlockEntity extends BlockEntity {

    public static final int LAVA = 50;
    public static final int MAGMA = 25;
    public static final int CAMPFIRE = 15;
    public static final long BASE_CAPACITY = 100_000;
    private static final long BASE_PUSH_PER_SIDE = 100;
    /** Ticks per flywheel turn. */
    public static final float CYCLE_TICKS = 40f;

    private long energy;
    /** Whether the block below is a heat source. Synced to the client, which spins the flywheel. */
    private boolean heated;

    /** Client-side: where the flywheel is in its turn. */
    public float animationTicks;

    /** The platform's energy handler for this engine, made once so transactions see one buffer. */
    @Nullable public Object energyHandler;

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("stirling_engine"), pos, state);
    }

    public StirlingEngineTier tier() {
        return getBlockState().getBlock() instanceof StirlingEngineBlock block ? block.tier() : StirlingEngineTier.BASIC;
    }

    public long capacity() {
        return BASE_CAPACITY * tier().multiplier();
    }

    public long energy() {
        return energy;
    }

    /** Sets the buffer directly; for the platform energy handlers, which manage transactions. */
    public void setEnergy(long energy) {
        if (energy != this.energy) {
            this.energy = energy;
            setChanged();
        }
    }

    public boolean isHeated() {
        return heated;
    }

    /** Energy per tick from the block below at this tier, or 0 if it isn't a heat source. */
    public int generation() {
        if (level == null) return 0;
        BlockState below = level.getBlockState(worldPosition.below());
        int base;
        if (below.is(Blocks.LAVA)) base = LAVA;
        else if (below.is(Blocks.MAGMA_BLOCK)) base = MAGMA;
        else if ((below.is(Blocks.CAMPFIRE) || below.is(Blocks.SOUL_CAMPFIRE)) && below.getValue(CampfireBlock.LIT))
            base = CAMPFIRE;
        else return 0;
        return base * tier().multiplier();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StirlingEngineBlockEntity be) {
        int generation = be.generation();
        boolean heated = generation > 0;
        if (heated != be.heated) {
            be.heated = heated;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (heated) be.setEnergy(Math.min(be.capacity(), be.energy + generation));
        if (be.energy > 0) {
            long pushed = StirlingEnginePlatform.pushEnergy(level, pos,
                    Math.min(be.energy, BASE_PUSH_PER_SIDE * be.tier().multiplier()));
            if (pushed > 0) be.setEnergy(be.energy - pushed);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, StirlingEngineBlockEntity be) {
        if (be.heated) be.animationTicks = (be.animationTicks + 1) % CYCLE_TICKS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("Energy", energy);
        output.putBoolean("Heated", heated);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = input.getLongOr("Energy", 0L);
        heated = input.getBooleanOr("Heated", false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Heated", heated);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
