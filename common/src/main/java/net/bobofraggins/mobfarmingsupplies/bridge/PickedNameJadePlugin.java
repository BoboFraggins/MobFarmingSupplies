package net.bobofraggins.mobfarmingsupplies.bridge;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Has Jade name these blocks after the item picking them gives, as newer Jade versions do for every
 * block: an Einstein-Rosen Bridge then shows its channel, and a Mob Head its mob. Jade 15 (1.21.1)
 * only does that for blocks a plugin asks for.
 */
@WailaPlugin
public class PickedNameJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.usePickedResult(Registration.EINSTEIN_ROSEN_BRIDGE.get());
        registration.usePickedResult(Registration.MOB_HEAD.get());
        registration.usePickedResult(Registration.MOB_WALL_HEAD.get());
    }
}
