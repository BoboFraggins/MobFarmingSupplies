package net.bobofraggins.mobfarmingsupplies.glamping.present;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Wrapping logic for the Present item, hooked via Architectury's
 * {@link InteractionEvent#USE_ITEM_ON_BLOCK} rather than a plain {@code Item#useOn} override.
 *
 * <p>Vanilla's own click dispatch tries the target block's reaction first ({@code useItemOn}/
 * {@code useWithoutItem}) before ever consulting the held item, so wrapping an "active" block
 * like a chest via a plain {@code Item#useOn} override would never fire — the chest would just
 * open. The original TremendousStorage implementation used NeoForge's {@code onItemUseFirst}
 * extension method (on {@code IItemExtension}) to run before the block got a chance, but that
 * method exists only on NeoForge's patched jar — it is not vanilla and does not exist in this
 * mod's cross-platform {@code common} module at all (confirmed absent from both the plain
 * vanilla jar and the NeoForge-patched one that {@code common} actually compiles against).
 * {@code USE_ITEM_ON_BLOCK} fires at that same earlier point on both loaders, so this event
 * reproduces the old NeoForge-only semantics without a per-loader split.
 */
public final class PresentWrapEvents {

    private PresentWrapEvents() {}

    public static void registerCommonEvents() {
        InteractionEvent.USE_ITEM_ON_BLOCK.register(PresentWrapEvents::onUseItemOnBlock);
    }

    private static EventResult onUseItemOnBlock(
            Level level, Player player, InteractionHand hand,
            ItemStack eventStack, BlockState state, BlockHitResult hit) {
        if (!(eventStack.getItem() instanceof PresentBlockItem)) return EventResult.pass();
        if (PresentBlockItem.hasWrappedBlock(eventStack)) return EventResult.pass();

        BlockPos pos = hit.getBlockPos();
        if (!canWrap(state, level, pos)) return EventResult.pass();

        if (!level.isClientSide()) {
            CompoundTag entityData = captureBlockEntity(level, pos);

            // UPDATE_SUPPRESS_DROPS only suppresses the replaced block's own item drop — a
            // container being replaced (chest, barrel, ...) still spills its inventory via
            // BlockEntity#preRemoveSideEffects's default Containers.dropContents() call unless
            // UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS is also set. entityData above already has a
            // full snapshot of the contents to restore, so vanilla's own spill would just dupe.
            Direction facing = player.getDirection().getOpposite();
            level.setBlock(
                    pos,
                    Registration.PRESENT.get().defaultBlockState().setValue(PresentBlock.FACING, facing),
                    Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);

            if (level.getBlockEntity(pos) instanceof PresentBlockEntity present) {
                present.setWrappedBlock(state, entityData);
            }

            if (!player.isCreative()) {
                // Mutating the event's own ItemStack parameter is not reliable — fetch the
                // live stack from the player and write the shrunk result back explicitly,
                // matching MagicHatCaptureEvents' established idiom for this exact reason.
                ItemStack heldStack = player.getItemInHand(hand);
                heldStack.shrink(1);
                player.setItemInHand(hand, heldStack);
            }
        }

        return EventResult.interruptTrue();
    }

    private static boolean canWrap(BlockState state, Level level, BlockPos pos) {
        if (state.isAir()) return false;
        if (state.getBlock() instanceof PresentBlock) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false;
        if (isMultiBlock(state)) return false;
        return true;
    }

    /**
     * Returns true when {@code state} is part of a structure that occupies more than one block
     * position, making it unsafe to wrap in a Present (would leave orphaned block halves or
     * cause item duplication with shared inventories).
     *
     * <ul>
     *   <li>Double chests — {@link ChestType} is LEFT or RIGHT (single chests are safe)
     *   <li>Beds — have a {@code BED_PART} property (HEAD / FOOT)
     *   <li>Doors and tall plants — have a {@code DOUBLE_BLOCK_HALF} property (UPPER / LOWER)
     * </ul>
     */
    private static boolean isMultiBlock(BlockState state) {
        // Double chests share an inventory across two positions; single chests are fine
        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)
                && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) {
            return true;
        }
        // Beds occupy two horizontal block positions (head + foot)
        if (state.hasProperty(BlockStateProperties.BED_PART)) return true;
        // Doors and tall plants occupy two vertical block positions (upper + lower)
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) return true;
        return false;
    }

    private static CompoundTag captureBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;
        TagValueOutput beOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        be.saveCustomOnly(beOut);
        CompoundTag saved = beOut.buildResult();
        return saved.isEmpty() ? null : saved;
    }
}
