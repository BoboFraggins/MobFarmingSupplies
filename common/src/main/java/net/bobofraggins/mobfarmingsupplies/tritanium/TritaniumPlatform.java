package net.bobofraggins.mobfarmingsupplies.tritanium;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

public final class TritaniumPlatform {

    private TritaniumPlatform() {}

    /** The Tritanium Paxel item: {@link TritaniumPaxelItem}, plus NeoForge's tool abilities there. */
    @ExpectPlatform
    public static TritaniumPaxelItem createPaxel(Item.Properties properties) {
        throw new AssertionError();
    }
}
