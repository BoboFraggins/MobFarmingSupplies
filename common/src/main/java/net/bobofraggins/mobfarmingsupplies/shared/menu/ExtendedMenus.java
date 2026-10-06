package net.bobofraggins.mobfarmingsupplies.shared.menu;

import dev.architectury.registry.menu.MenuRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.Optional;

/**
 * Opens Architectury "extended" menus with typed extra data. The client-side menu constructor
 * (registered via {@code MenuRegistry.ofExtended(factory)}) decodes the buffer with the same codec.
 */
public final class ExtendedMenus {

    /** A block position that may be absent — e.g. a Picnic Basket opened from an item. */
    public static final StreamCodec<ByteBuf, Optional<BlockPos>> OPTIONAL_POS =
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC);

    private ExtendedMenus() {}

    /** Opens {@code provider}'s menu, sending {@code data} to the client-side menu via {@code codec}. */
    public static <D> void open(ServerPlayer player, MenuProvider provider, D data,
                                StreamCodec<? super FriendlyByteBuf, D> codec) {
        MenuRegistry.openExtendedMenu(player, provider, buf -> codec.encode(buf, data));
    }
}
