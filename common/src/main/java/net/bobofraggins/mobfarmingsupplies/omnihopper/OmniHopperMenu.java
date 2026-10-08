package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.bobofraggins.mobfarmingsupplies.logisticsorter.SorterFilters;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Omnidirectional Hopper.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>0–8 — Item Filter slots (one filter each)
 *   <li>9–35 — player inventory
 *   <li>36–44 — player hotbar
 * </ul>
 * Side modes and AND/OR are not slots; the screen reads them from the client block entity and
 * sends changes with {@link net.bobofraggins.mobfarmingsupplies.network.SetOmniHopperConfigPacket}.
 */
public class OmniHopperMenu extends AbstractContainerMenu {

    // Vertical layout (relative to topPos) — must match OmniHopperScreen's panes:
    // title(17) + gap(4) + sides(90) + gap(4) + mode(20) + gap(4) = 139
    public static final int FILTER_LEFT = 8;
    public static final int FILTER_TOP  = 139;
    // + filter row(18) + gap(6) = 163
    public static final int PLAYER_SLOT_LEFT = 8;
    public static final int INV_TOP    = 163;
    public static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4; // 221

    private static final int FILTERS   = OmniHopperBlockEntity.FILTER_SLOTS;
    private static final int INV_FIRST = FILTERS;
    private static final int SLOT_END  = FILTERS + 36;

    private final BlockPos pos;
    private final Container filters;
    private final Block block;

    /** Server-side constructor. */
    public OmniHopperMenu(int syncId, Inventory inv, OmniHopperBlockEntity be) {
        this(syncId, inv, be.getBlockPos(), be.getFilters());
    }

    /** Client-side constructor (via MenuRegistry.ofExtended). */
    public OmniHopperMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, buf.readBlockPos(), new SimpleContainer(FILTERS));
    }

    private OmniHopperMenu(int syncId, Inventory inv, BlockPos pos, Container filters) {
        this(Registration.OMNI_HOPPER_MENU.get(), Registration.OMNI_HOPPER.get(), syncId, inv, pos, filters);
    }

    /**
     * Shared by the Einstein-Rosen Bridge's menu, which has the same layout.
     *
     * @param block the block this menu belongs to (closes when it's gone)
     */
    protected OmniHopperMenu(MenuType<?> type, Block block, int syncId, Inventory inv, BlockPos pos, Container filters) {
        super(type, syncId);
        this.pos = pos;
        this.filters = filters;
        this.block = block;

        for (int i = 0; i < FILTERS; i++) {
            addSlot(new Slot(filters, i, FILTER_LEFT + i * 18, FILTER_TOP) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return SorterFilters.isFilter(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, PLAYER_SLOT_LEFT + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, PLAYER_SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    public BlockPos getPos() { return pos; }

    /** The filter slots' container (on the client, kept in sync through the slots). */
    public Container getFilters() { return filters; }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64
                && player.level().getBlockState(pos).is(block);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack    = slot.getItem();
        ItemStack original = stack.copy();

        if (index < FILTERS) {
            if (!moveItemStackTo(stack, INV_FIRST, SLOT_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, FILTERS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }
}
