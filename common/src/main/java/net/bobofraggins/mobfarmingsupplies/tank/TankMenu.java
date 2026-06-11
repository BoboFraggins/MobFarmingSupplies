package net.bobofraggins.mobfarmingsupplies.tank;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Tank's fill/drain UI.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>0 — fluid-container input (accepts any item exposing the platform's fluid-item storage)
 *   <li>1 — fluid-container output (output-only; receives the emptied/filled container)
 *   <li>2–28 — player inventory
 *   <li>29–37 — player hotbar
 * </ul>
 */
public class TankMenu extends AbstractContainerMenu {

    // Fluid-transfer slot positions — centred in the 176 px panel: (176-16)/2 = 80
    public static final int FLUID_IN_X  = 80;
    public static final int FLUID_IN_Y  = 20;
    public static final int FLUID_OUT_X = 80;
    public static final int FLUID_OUT_Y = 62;

    // Player-inventory slot positions: (176-162)/2 = 7
    // Y values: TITLE_H(17) + settings pane(71) = 88
    public static final int INV_START_X = 7;
    public static final int INV_Y       = 88;
    public static final int HOTBAR_Y    = INV_Y + 3 * 18 + 4; // 146

    private final BlockPos pos;

    /** Server-side constructor — called from {@link TankBlockEntity#createMenu}. */
    public TankMenu(int windowId, Inventory inv, BlockPos pos, SimpleContainer transferContainer) {
        super(Registration.TANK_MENU.get(), windowId);
        this.pos = pos;

        // Slot 0: fluid input — any item exposing the platform's fluid-item storage
        addSlot(new Slot(transferContainer, 0, FLUID_IN_X, FLUID_IN_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return TankBlockEntityPlatform.isFluidContainer(stack);
            }
        });

        // Slot 1: output-only — player takes the processed container, cannot place
        addSlot(new Slot(transferContainer, 1, FLUID_OUT_X, FLUID_OUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player inventory (slots 2–28)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_START_X + col * 18, INV_Y + row * 18));
            }
        }
        // Hotbar (slots 29–37)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    /** Client-side constructor — reads BlockPos from network buffer; creates a dummy transfer container. */
    public TankMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(2));
    }

    public BlockPos getPos() { return pos; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack    = slot.getItem();
        ItemStack original = stack.copy();

        if (index < 2) {
            // Fluid slots → player inventory + hotbar
            if (!moveItemStackTo(stack, 2, 38, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            // Player inventory → try fluid input first, then hotbar
            if (!moveItemStackTo(stack, 0, 1, false)) {
                if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
            }
        } else {
            // Hotbar → try fluid input first, then player inventory
            if (!moveItemStackTo(stack, 0, 1, false)) {
                if (!moveItemStackTo(stack, 2, 29, false)) return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else if (stack.getCount() != original.getCount()) slot.setChanged();
        else return ItemStack.EMPTY;

        return original;
    }
}
