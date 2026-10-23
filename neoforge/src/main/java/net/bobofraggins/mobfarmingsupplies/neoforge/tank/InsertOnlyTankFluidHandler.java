package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A block's experience tank to pipes, for blocks that only use experience (the XP Powered
 * Incubator, the XP Powered Anvil): it goes in, nothing comes out.
 */
public class InsertOnlyTankFluidHandler extends TankFluidHandler {

    public InsertOnlyTankFluidHandler(TankBlockEntity be) {
        super(be);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
