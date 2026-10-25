package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntityPlatform;
import net.bobofraggins.mobfarmingsupplies.tank.TankBottleTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Experience Pylon: the tank's two fluid-container slots, plus the Store and
 * Retrieve buttons, which arrive as menu button clicks ({@link #clickMenuButton}).
 *
 * <p>Slots: 0 container in, 1 container out, 2-28 player inventory, 29-37 hotbar.
 */
public class ExperiencePylonMenu extends AbstractContainerMenu {

    // The middle column, centred in the 176 px panel.
    public static final int FLUID_IN_X  = 80;
    public static final int FLUID_IN_Y  = 44;
    public static final int FLUID_OUT_X = 80;
    public static final int FLUID_OUT_Y = 82;

    public static final int SETTINGS_PANE_H = 87;
    public static final int INV_START_X = 7;
    public static final int INV_Y       = 17 + SETTINGS_PANE_H; // below the title bar and the pane
    public static final int HOTBAR_Y    = INV_Y + 3 * 18 + 4;

    /** Button ids: store 1, 5, all levels, then retrieve 1, 5, all. */
    public static final int STORE_1 = 0, STORE_5 = 1, STORE_ALL = 2;
    public static final int RETRIEVE_1 = 3, RETRIEVE_5 = 4, RETRIEVE_ALL = 5;

    private final BlockPos pos;
    private final ContainerLevelAccess access;

    /** Server side, from {@link ExperiencePylonBlockEntity#createMenu}. */
    public ExperiencePylonMenu(int windowId, Inventory inv, BlockPos pos, SimpleContainer transferContainer) {
        super(Registration.EXPERIENCE_PYLON_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        addSlot(new Slot(transferContainer, 0, FLUID_IN_X, FLUID_IN_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return TankBlockEntityPlatform.isFluidContainer(stack) || TankBottleTransfer.isBottle(stack);
            }
        });
        addSlot(new Slot(transferContainer, 1, FLUID_OUT_X, FLUID_OUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_START_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_START_X + col * 18, HOTBAR_Y));
        }
    }

    /** Client side: the position comes over the network, the slots are a stand-in container. */
    public ExperiencePylonMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(2));
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level().getBlockEntity(pos) instanceof ExperiencePylonBlockEntity pylon)) {
            return false;
        }
        switch (id) {
            case STORE_1      -> PylonExperience.store(pylon, serverPlayer, 1);
            case STORE_5      -> PylonExperience.store(pylon, serverPlayer, 5);
            case STORE_ALL    -> PylonExperience.store(pylon, serverPlayer, PylonExperience.ALL);
            case RETRIEVE_1   -> PylonExperience.retrieve(pylon, serverPlayer, 1);
            case RETRIEVE_5   -> PylonExperience.retrieve(pylon, serverPlayer, 5);
            case RETRIEVE_ALL -> PylonExperience.retrieve(pylon, serverPlayer, PylonExperience.ALL);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.EXPERIENCE_PYLON.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack    = slot.getItem();
        ItemStack original = stack.copy();

        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
            }
        } else {
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
