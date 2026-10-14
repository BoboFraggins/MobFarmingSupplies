package net.bobofraggins.mobfarmingsupplies.witherinhibitor;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorRegistry;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The Wither Inhibitor's block entity: the Ender Inhibitor's, recording itself as a Wither Inhibitor. */
public class WitherInhibitorBlockEntity extends EnderInhibitorBlockEntity {

    public WitherInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("wither_inhibitor"), pos, state);
    }

    @Override
    protected InhibitorRegistry registry() {
        return InhibitorRegistry.WITHER;
    }
}
