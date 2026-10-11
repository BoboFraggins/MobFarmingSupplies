package net.bobofraggins.mobfarmingsupplies.stirlingengine.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class StirlingEnginePlatformImpl {

    private StirlingEnginePlatformImpl() {}

    public static long pushEnergy(Level level, BlockPos pos, long perSide) {
        long pushed = 0;
        for (Direction dir : Direction.values()) {
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(dir), dir.getOpposite());
            if (target == null) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int accepted = target.insert((int) Math.min(perSide, Integer.MAX_VALUE), tx);
                tx.commit();
                pushed += accepted;
            }
        }
        return pushed;
    }
}
