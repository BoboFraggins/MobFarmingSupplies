package net.bobofraggins.mobfarmingsupplies.stirlingengine.fabric;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

public final class StirlingEnginePlatformImpl {

    private StirlingEnginePlatformImpl() {}

    public static long pushEnergy(Level level, BlockPos pos, long perSide) {
        long pushed = 0;
        for (Direction dir : Direction.values()) {
            EnergyStorage target = EnergyStorage.SIDED.find(level, pos.relative(dir), dir.getOpposite());
            if (target == null || !target.supportsInsertion()) continue;
            try (Transaction tx = Transaction.openOuter()) {
                long accepted = target.insert(perSide, tx);
                tx.commit();
                pushed += accepted;
            }
        }
        return pushed;
    }
}
