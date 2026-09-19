package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.network.OpenPicnicBasketPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Client-side keybind to open a worn/carried Picnic Basket, needed because an equipped
 * accessory-slot item can't be right-clicked. Registration ({@link KeyMappingRegistry}) and
 * the per-tick consume-and-send loop are both cross-platform via Architectury, so no
 * per-loader code is required.
 */
public final class PicnicBasketKeybinds {

    private PicnicBasketKeybinds() {}

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "category"));

    public static final KeyMapping OPEN_BASKET = new KeyMapping(
            "key.mobfarmingsupplies.open_picnic_basket",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_B,
            CATEGORY);

    public static void init() {
        KeyMappingRegistry.register(OPEN_BASKET);
        ClientTickEvent.CLIENT_POST.register(mc -> {
            while (OPEN_BASKET.consumeClick()) {
                NetworkManager.sendToServer(new OpenPicnicBasketPacket());
            }
        });
    }
}
