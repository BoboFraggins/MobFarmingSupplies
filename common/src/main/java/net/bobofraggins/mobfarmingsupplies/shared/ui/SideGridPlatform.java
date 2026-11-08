package net.bobofraggins.mobfarmingsupplies.shared.ui;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Loader-specific pick-block lookup for the {@link SideGridPane}'s neighbour icons. */
public final class SideGridPlatform {

    private SideGridPlatform() {}

    /**
     * The item a player would get by pick-blocking (middle-clicking) {@code state} at {@code pos}.
     * Goes through NeoForge's pick-block hook there, which is what many modded pipes, cables and
     * multipart blocks override because they have no item of their own.
     */
    @ExpectPlatform
    public static ItemStack pickBlock(Level level, BlockPos pos, BlockState state, Player player) {
        throw new AssertionError("Missing platform implementation");
    }
}
