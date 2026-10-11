package net.bobofraggins.mobfarmingsupplies.tritanium.neoforge;

import net.bobofraggins.mobfarmingsupplies.neoforge.tritanium.TritaniumPaxelItemNeoForge;
import net.bobofraggins.mobfarmingsupplies.tritanium.TritaniumPaxelItem;
import net.minecraft.world.item.Item;

public final class TritaniumPlatformImpl {

    private TritaniumPlatformImpl() {}

    public static TritaniumPaxelItem createPaxel(Item.Properties properties) {
        return new TritaniumPaxelItemNeoForge(properties);
    }
}
