package net.bobofraggins.mobfarmingsupplies.guide;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/**
 * The in-game guide: a Modonomicon book defined under
 * {@code data/mobfarmingsupplies/modonomicon/books/guide}. It only exists when Modonomicon is
 * installed; the mod has no code dependency on it, so the book item is built from its ids.
 */
public final class GuideBook {

    public static final String MODONOMICON = "modonomicon";

    private GuideBook() {}

    /** The guide book item, or empty if Modonomicon isn't installed. */
    public static ItemStack stack(HolderLookup.Provider registries) {
        if (!Platform.isModLoaded(MODONOMICON)) return ItemStack.EMPTY;
        JsonObject components = new JsonObject();
        components.addProperty("modonomicon:book_id", "mobfarmingsupplies:guide");
        JsonObject json = new JsonObject();
        json.addProperty("id", "modonomicon:modonomicon");
        json.add("components", components);
        return ItemStack.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json)
                .result().orElse(ItemStack.EMPTY);
    }
}
