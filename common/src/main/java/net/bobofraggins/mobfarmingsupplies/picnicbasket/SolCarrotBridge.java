package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Optional integration with Spice of Life: Carrot Edition's "prefer unfamiliar foods" tracking,
 * used by {@link PicnicBasketFeedHandler} to bias auto-feed towards food the player hasn't
 * eaten yet.
 *
 * <p>NeoForge-only — the integration reflects into NeoForge-specific
 * {@code net.neoforged.neoforge.attachment.AttachmentType} internals, matching the source mod's
 * approach. The Fabric implementation is a permanent no-op passthrough.
 */
public final class SolCarrotBridge {

    private SolCarrotBridge() {}

    /**
     * Returns the subset of {@code candidates} the player hasn't eaten yet according to SoLC.
     * Returns {@code candidates} unchanged if none qualify, or if SoLC isn't installed.
     */
    @ExpectPlatform
    public static List<ItemStack> preferUneatenFoods(Player player, List<ItemStack> candidates) {
        throw new AssertionError("Missing platform implementation");
    }
}
