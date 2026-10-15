package net.bobofraggins.mobfarmingsupplies.magnet;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.bobofraggins.mobfarmingsupplies.network.ToggleMagnetPacket;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketKeybinds;
import net.minecraft.client.KeyMapping;

/** A key, unbound until the player chooses one, that switches their magnet on or off. */
public final class MagnetKeybinds {

    private MagnetKeybinds() {}

    public static final KeyMapping TOGGLE_MAGNET = new KeyMapping(
            "key.mobfarmingsupplies.toggle_magnet",
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            PicnicBasketKeybinds.CATEGORY);

    public static void init() {
        KeyMappingRegistry.register(TOGGLE_MAGNET);
        ClientTickEvent.CLIENT_POST.register(mc -> {
            while (TOGGLE_MAGNET.consumeClick()) {
                NetworkManager.sendToServer(new ToggleMagnetPacket());
            }
        });
    }
}
