package net.bobofraggins.mobfarmingsupplies.blockbreaker;

import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

/**
 * Menu for the Block Breaker: the Omnidirectional Hopper's (nine Item Filter slots, then the
 * player's inventory), {@link #EXTRA_TOP} lower for the Silk Touch row above the filters.
 */
public class BlockBreakerMenu extends OmniHopperMenu {

    /** The Silk Touch row and the gap after it, between the side grid and the AND / OR toggle. */
    public static final int EXTRA_TOP = BlockBreakerSilkTouchPane.HEIGHT + 4;

    /** Server-side constructor. */
    public BlockBreakerMenu(int syncId, Inventory inv, BlockBreakerBlockEntity be) {
        this(syncId, inv, be.getBlockPos(), be.getFilters());
    }

    /** Client-side constructor (via MenuRegistry.ofExtended). */
    public BlockBreakerMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, buf.readBlockPos(), new SimpleContainer(BlockBreakerBlockEntity.FILTER_SLOTS));
    }

    private BlockBreakerMenu(int syncId, Inventory inv, BlockPos pos, Container filters) {
        super(Registration.BLOCK_BREAKER_MENU.get(), Registration.BLOCK_BREAKER.get(), syncId, inv, pos, filters,
                EXTRA_TOP);
    }
}
