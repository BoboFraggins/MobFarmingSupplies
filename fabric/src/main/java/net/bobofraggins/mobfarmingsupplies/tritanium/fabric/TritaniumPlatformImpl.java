package net.bobofraggins.mobfarmingsupplies.tritanium.fabric;

import net.bobofraggins.mobfarmingsupplies.tritanium.TritaniumPaxelItem;
import net.minecraft.world.item.Item;

public final class TritaniumPlatformImpl {

    private TritaniumPlatformImpl() {}

    public static TritaniumPaxelItem createPaxel(Item.Properties properties) {
        return new TritaniumPaxelItem(properties);
    }
}
