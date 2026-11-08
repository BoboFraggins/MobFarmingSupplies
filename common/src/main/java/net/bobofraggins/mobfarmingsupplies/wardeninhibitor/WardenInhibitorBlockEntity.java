package net.bobofraggins.mobfarmingsupplies.wardeninhibitor;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorRegistry;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The Warden Inhibitor's block entity: the Ender Inhibitor's, recording itself as a Warden Inhibitor. */
public class WardenInhibitorBlockEntity extends EnderInhibitorBlockEntity {

    public WardenInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("warden_inhibitor"), pos, state);
    }

    @Override
    protected InhibitorRegistry registry() {
        return InhibitorRegistry.WARDEN;
    }
}
