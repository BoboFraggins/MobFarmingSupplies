package net.bobofraggins.mobfarmingsupplies.filterscribingterminal;

import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemFilterData;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatcher;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemMatchers;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.HolderLookup;

/**
 * Container menu for the Filter Scribing Terminal.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>0 — Blank Filter input (left)
 *   <li>1 — scribed Item Filter output (right; take-only)
 *   <li>2–28 — player inventory
 *   <li>29–37 — player hotbar
 * </ul>
 *
 * <p>The input is the terminal block entity's container, so Blank Filters stay in the terminal
 * when the menu closes and are shared by everyone using it. The output is a result slot the
 * server recomputes whenever the input or the scribing controls change
 * ({@link #setScribingState}, sent by the screen); every open menu listens to the shared input
 * so all viewers' outputs stay current.
 * Taking the output consumes one Blank Filter. The menu's open/close lifecycle also drives
 * the terminal's {@link FilterScribingTerminalBlock#ACTIVE} screen state.
 */
public class FilterScribingTerminalMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT  = 0;
    public static final int OUTPUT_SLOT = 1;

    // Terminal slots, vertically centred beside the 96 px controls pane (ScribingControlsPane)
    // under the title bar: TITLE_H(17) + (96 - 16) / 2 = 57.
    public static final int INPUT_X  = 17;
    public static final int OUTPUT_X = 176 - 17 - 16; // 143
    public static final int SLOT_Y   = 57;

    // Player-inventory slot positions: (176-162)/2 = 7; TITLE_H(17) + controls pane(96) = 113
    public static final int INV_START_X = 7;
    public static final int INV_Y       = 113;
    public static final int HOTBAR_Y    = INV_Y + 3 * 18 + 4; // 171

    private static final int INV_FIRST    = 2;
    private static final int HOTBAR_FIRST = 29;
    private static final int SLOT_END     = 38;

    private final BlockPos pos;
    private final ContainerLevelAccess access;
    @Nullable
    private final FilterScribingTerminalBlockEntity terminal;
    private final SimpleContainer input;
    private final Runnable inputListener = this::onInputChanged;
    private final ResultContainer result = new ResultContainer();

    // Scribing controls, mirrored from the client screen (server side only).
    private ItemStack matcherItem = ItemStack.EMPTY;
    private boolean negate = false;
    private int selected = -1;

    // ── Server-side constructor ──────────────────────────────────────────────────

    public FilterScribingTerminalMenu(
            int syncId, Inventory playerInv, FilterScribingTerminalBlockEntity terminal, ContainerLevelAccess access) {
        this(syncId, playerInv, terminal.getBlockPos(), access, terminal, terminal.getInput());
        terminal.addInputListener(inputListener);
    }

    // ── Client-side constructor (via MenuRegistry.ofExtended, BlockPos.STREAM_CODEC) ──

    public FilterScribingTerminalMenu(int syncId, Inventory playerInv, BlockPos pos) {
        this(syncId, playerInv, pos, ContainerLevelAccess.NULL, null, new SimpleContainer(1));
    }

    private FilterScribingTerminalMenu(int syncId, Inventory playerInv, BlockPos pos, ContainerLevelAccess access,
            @Nullable FilterScribingTerminalBlockEntity terminal, SimpleContainer input) {
        super(Registration.FILTER_SCRIBING_TERMINAL_MENU.get(), syncId);
        this.pos = pos;
        this.access = access;
        this.terminal = terminal;
        this.input = input;

        addSlot(new Slot(input, 0, INPUT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Registration.BLANK_FILTER.get());
            }
        });
        addSlot(new Slot(result, 0, OUTPUT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                input.removeItem(0, 1);
                updateResult();
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, INV_START_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    public BlockPos getPos() { return pos; }

    // ── Scribing ──────────────────────────────────────────────────────────────────

    /**
     * Applies the screen's control state (server side). The row index is resolved against
     * the matcher list rebuilt here from the item, so the client can't request a matcher the
     * item doesn't offer.
     */
    public void setScribingState(ItemStack matcher, boolean negate, int selected) {
        this.matcherItem = matcher.isEmpty() ? ItemStack.EMPTY : matcher.copyWithCount(1);
        this.negate = negate;
        this.selected = selected;
        updateResult();
    }

    private void onInputChanged() {
        slotsChanged(input);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == input) updateResult();
    }

    /** Recomputes the output slot. Server only — the client receives the result via slot sync. */
    private void updateResult() {
        access.execute((level, p) -> result.setItem(0, buildResult(level.registryAccess())));
    }

    private ItemStack buildResult(HolderLookup.Provider registries) {
        if (!input.getItem(0).is(Registration.BLANK_FILTER.get())) return ItemStack.EMPTY;
        List<ItemMatcher> matchers = ItemMatchers.forItem(matcherItem, registries);
        if (selected < 0 || selected >= matchers.size()) return ItemStack.EMPTY;
        ItemStack filter = new ItemStack(Registration.ITEM_FILTER.get());
        filter.set(Registration.ITEM_FILTER_DATA.get(), new ItemFilterData(matchers.get(selected), negate));
        return filter;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player player) {
        // Closes the UI if the terminal is broken or the player walks away.
        return stillValid(access, player, Registration.FILTER_SCRIBING_TERMINAL.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Blank Filters stay in the terminal; the output is only a preview, so it just vanishes.
        if (terminal != null) terminal.removeInputListener(inputListener);
        if (player.level() instanceof ServerLevel level) {
            FilterScribingTerminalBlock.onMenuClosed(level, pos, this);
        }
    }

    /** Double-click "collect all" must not take (and so scribe) the output preview — as CraftingMenu does. */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack, slot);
    }

    // ── Shift-click ───────────────────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack    = slot.getItem();
        ItemStack original = stack.copy();

        if (index == OUTPUT_SLOT) {
            // Move just one filter; returning EMPTY stops the caller's repeat-until-done loop.
            if (moveItemStackTo(stack, INV_FIRST, SLOT_END, true)) slot.onTake(player, original);
            return ItemStack.EMPTY;
        }

        if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, INV_FIRST, SLOT_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            // Not a Blank Filter (or input full) — shuffle between inventory and hotbar
            boolean moved = index < HOTBAR_FIRST
                    ? moveItemStackTo(stack, HOTBAR_FIRST, SLOT_END, false)
                    : moveItemStackTo(stack, INV_FIRST, HOTBAR_FIRST, false);
            if (!moved) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }
}
