package net.bobofraggins.mobfarmingsupplies.neoforge.incubator;

import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.neoforge.tank.TankFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** The XP Powered Incubator's experience tank to pipes: experience goes in, nothing comes out. */
public class IncubatorFluidHandler extends TankFluidHandler {

    public IncubatorFluidHandler(IncubatorBlockEntity be) {
        super(be);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        return 0;
    }
}
