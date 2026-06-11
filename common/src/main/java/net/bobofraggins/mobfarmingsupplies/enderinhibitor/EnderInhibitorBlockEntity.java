package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Ender Inhibitor.
 *
 * <p>Carries no persistent data — it exists solely to give the block a
 * {@link EnderInhibitorBlockEntityRenderer} attachment point so the rotating
 * Ender Pearl can be rendered above the prong tips, and to register/unregister
 * the block's position with {@link EnderInhibitorEvents} so teleport suppression
 * can use an O(k) distance check rather than a block scan.
 */
public class EnderInhibitorBlockEntity extends BlockEntity {

    public EnderInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("ender_inhibitor"), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            EnderInhibitorEvents.addInhibitor(level, worldPosition);
        }
    }

    // Called by NeoForge IBlockEntityExtension when the chunk containing this BE unloads.
    // No @Override because onChunkUnloaded() is a NeoForge extension, not vanilla.
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            EnderInhibitorEvents.removeInhibitor(level, worldPosition);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            EnderInhibitorEvents.removeInhibitor(level, worldPosition);
        }
        super.preRemoveSideEffects(pos, state);
    }
}
