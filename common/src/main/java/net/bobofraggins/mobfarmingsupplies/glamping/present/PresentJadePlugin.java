package net.bobofraggins.mobfarmingsupplies.glamping.present;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (WAILA) plugin for the Present — shows the wrapped block's name in the tooltip, if any.
 * Same auto-discovery mechanism as {@code TankJadePlugin}: the {@link WailaPlugin} annotation
 * is picked up automatically, no service-file registration needed.
 */
@WailaPlugin
public class PresentJadePlugin implements IWailaPlugin {

    static final Identifier PROVIDER = Identifier.fromNamespaceAndPath("mobfarmingsupplies", "present");

    private static final String KEY_WRAPPED_BLOCK = "WrappedBlock";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ServerProvider.INSTANCE, PresentBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ClientProvider.INSTANCE, PresentBlock.class);
    }

    enum ServerProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof PresentBlockEntity present)) return;
            var wrapped = present.getWrappedState();
            if (wrapped == null) return;
            Identifier id = BuiltInRegistries.BLOCK.getKey(wrapped.getBlock());
            if (id != null) data.putString(KEY_WRAPPED_BLOCK, id.toString());
        }

        @Override
        public Identifier getUid() {
            return PROVIDER;
        }
    }

    enum ClientProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            var blockId = data.getString(KEY_WRAPPED_BLOCK);
            if (blockId.isEmpty()) return;

            Identifier rl = Identifier.tryParse(blockId.get());
            if (rl == null) return;

            Block block = BuiltInRegistries.BLOCK.getValue(rl);
            Item item = block.asItem();
            Component wrappedName = item == Items.AIR ? block.getName() : item.getName(new ItemStack(item));

            Component baseName = Component.translatable(accessor.getBlock().getDescriptionId());
            tooltip.remove(JadeIds.MC_BLOCK_DISPLAY);
            tooltip.add(0, Component.empty().append(baseName).append(" (").append(wrappedName).append(")"));
        }

        @Override
        public Identifier getUid() {
            return PROVIDER;
        }
    }
}
