package net.bobofraggins.mobfarmingsupplies.filterscribingterminal;

import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueInput;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the Filter Scribing Terminal's Blank Filter input so it persists between uses.
 * The slot is shared: every player with the terminal open sees the same stack, and each open
 * menu registers a listener so its output refreshes when anyone changes the input.
 */
public class FilterScribingTerminalBlockEntity extends BlockEntity {

    private static final String TAG_INPUT = "Input";

    private final List<Runnable> inputListeners = new ArrayList<>();
    private final SimpleContainer input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            FilterScribingTerminalBlockEntity.this.setChanged();
            // Copy: a listener may close its menu (and unregister) while we iterate.
            for (Runnable listener : List.copyOf(inputListeners)) listener.run();
        }
    };

    public FilterScribingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("filter_scribing_terminal"), pos, state);
    }

    public SimpleContainer getInput() { return input; }

    public void addInputListener(Runnable listener) { inputListeners.add(listener); }

    public void removeInputListener(Runnable listener) { inputListeners.remove(listener); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        output.store(TAG_INPUT, MFSCodecs.OPTIONAL_ITEM_STACK, input.getItem(0));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput in = new ValueInput(tag);
        input.setItem(0, in.read(TAG_INPUT, MFSCodecs.OPTIONAL_ITEM_STACK).orElse(ItemStack.EMPTY));
    }

    /** Called by the block when it is removed (see its {@code onRemove}). */
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, input);
        }
    }
}
