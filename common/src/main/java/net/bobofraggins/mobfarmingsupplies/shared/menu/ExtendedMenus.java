package net.bobofraggins.mobfarmingsupplies.shared.menu;

import dev.architectury.registry.menu.ExtendedMenuDataProvider;
import dev.architectury.registry.menu.MenuRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Opens Architectury "extended" menus with typed extra data, replacing the deprecated
 * {@code MenuRegistry.openExtendedMenu(player, provider, buf -> ...)} form. The matching menu
 * types are registered with {@code MenuRegistry.ofExtended(factory, codec)} using the same codec.
 */
public final class ExtendedMenus {

    /** A block position that may be absent — e.g. a Picnic Basket opened from an item. */
    public static final StreamCodec<ByteBuf, Optional<BlockPos>> OPTIONAL_POS =
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC);

    private ExtendedMenus() {}

    /** Opens {@code provider}'s menu, sending the block position to the client-side menu. */
    public static void openAt(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        open(player, provider, pos, BlockPos.STREAM_CODEC);
    }

    /** Opens {@code provider}'s menu, sending {@code data} to the client-side menu via {@code codec}. */
    public static <D> void open(ServerPlayer player, MenuProvider provider, D data,
                                StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        MenuRegistry.openExtendedMenu(player, new ExtendedMenuDataProvider<D>() {
            @Override
            public D getExtraData(ServerPlayer p) { return data; }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, D> getExtraDataCodec() { return codec; }

            @Override
            public Component getDisplayName() { return provider.getDisplayName(); }

            @Override
            @Nullable
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return provider.createMenu(syncId, inv, p);
            }
        });
    }
}
