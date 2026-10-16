package net.bobofraggins.mobfarmingsupplies.bridge;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Has Jade name these blocks after the item picking them gives: an Einstein-Rosen Bridge then shows
 * its channel, and a Mob Head its mob. Jade only does that for blocks on its pick list.
 */
@WailaPlugin
public class PickedNameJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        pick(registration, Registration.EINSTEIN_ROSEN_BRIDGE.get());
        pick(registration, Registration.MOB_HEAD.get());
        pick(registration, Registration.MOB_WALL_HEAD.get());
    }

    private static void pick(IWailaCommonRegistration registration, Block block) {
        BuiltInRegistries.BLOCK.getResourceKey(block).ifPresent(registration.blockOperations()::pick);
    }
}
