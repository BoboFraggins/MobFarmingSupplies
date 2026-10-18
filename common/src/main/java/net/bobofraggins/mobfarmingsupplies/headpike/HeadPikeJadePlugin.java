package net.bobofraggins.mobfarmingsupplies.headpike;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (WAILA) plugin for the Head Pike: what it does, and which mobs it's keeping away. The skulls
 * are already on the client (the pike syncs them to draw them), so that needs no server data.
 * Looking at the top half is treated as looking at the bottom one, which holds the skulls, so
 * Jade shows the same everywhere on the pike (its own list of the skulls included).
 */
@WailaPlugin
public class HeadPikeJadePlugin implements IWailaPlugin {

    static final Identifier PROVIDER = Identifier.fromNamespaceAndPath("mobfarmingsupplies", "head_pike");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ClientProvider.INSTANCE, HeadPikeBlock.class);
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (!(accessor instanceof BlockAccessor block) || !block.getBlockState().is(Registration.HEAD_PIKE.get())
                    || block.getBlockState().getValue(HeadPikeBlock.HALF) != DoubleBlockHalf.UPPER) return accessor;
            BlockPos lower = block.getPosition().below();
            BlockState lowerState = block.getLevel().getBlockState(lower);
            if (!lowerState.is(Registration.HEAD_PIKE.get())) return accessor;
            return registration.blockAccessor().from(block)
                    .hit(block.getHitResult().withPosition(lower))
                    .blockState(lowerState)
                    .blockEntity(block.getLevel().getBlockEntity(lower))
                    .build();
        });
    }

    enum ClientProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("item.mobfarmingsupplies.head_pike.tooltip").withStyle(ChatFormatting.GRAY));
            if (!(accessor.getBlockEntity() instanceof HeadPikeBlockEntity pike)) return;
            List<EntityType<?>> warded = pike.warded();
            if (warded.isEmpty()) return;
            MutableComponent names = Component.empty();
            for (int i = 0; i < warded.size(); i++) {
                if (i > 0) names.append(", ");
                names.append(warded.get(i).getDescription());
            }
            tooltip.add(Component.translatable("jade.mobfarmingsupplies.head_pike.warding", names));
        }

        @Override
        public Identifier getUid() {
            return PROVIDER;
        }
    }
}
