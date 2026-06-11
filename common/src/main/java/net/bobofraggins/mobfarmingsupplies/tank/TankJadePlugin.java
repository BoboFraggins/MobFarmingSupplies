package net.bobofraggins.mobfarmingsupplies.tank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import dev.architectury.fluid.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (WAILA) plugin for the MGR Tank.
 *
 * <p>When Jade is present, looking at a Tank shows the stored fluid name and
 * current / maximum amount, e.g. "8,000 / 16,000 mB (Water)" or "Empty".
 * The plugin is discovered automatically via the {@link WailaPlugin} annotation;
 * no service-file registration is required.
 *
 * <p>Jade is an optional dependency — this class is only loaded when Jade is
 * present. Nothing in the main mod code references it directly.
 */
@WailaPlugin
public class TankJadePlugin implements IWailaPlugin {

    static final Identifier TANK_PROVIDER =
            Identifier.fromNamespaceAndPath("mobfarmingsupplies", "tank");

    private static final String KEY_AMOUNT   = "Amount";
    private static final String KEY_CAPACITY = "Capacity";
    private static final String KEY_FLUID    = "Fluid";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TankServerProvider.INSTANCE, TankBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TankClientProvider.INSTANCE, TankBlock.class);
    }

    // ── Server-side data provider ─────────────────────────────────────────────

    enum TankServerProvider implements snownee.jade.api.IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof TankBlockEntity be)) return;
            data.putLong(KEY_AMOUNT, be.getAmount());
            data.putLong(KEY_CAPACITY, be.getCapacity());
            FluidStack fluid = be.getStoredFluid();
            if (!fluid.isEmpty()) {
                net.minecraft.nbt.Tag fluidTag = FluidStack.CODEC
                        .encodeStart(
                                accessor.getLevel().registryAccess()
                                        .createSerializationContext(NbtOps.INSTANCE),
                                fluid)
                        .result()
                        .orElse(new CompoundTag());
                data.put(KEY_FLUID, fluidTag);
            }
        }

        @Override
        public Identifier getUid() {
            return TANK_PROVIDER;
        }
    }

    // ── Client-side component provider ────────────────────────────────────────

    enum TankClientProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();

            if (!data.contains(KEY_FLUID)) {
                tooltip.add(Component.translatable("jade.mobfarmingsupplies.tank.empty"));
                return;
            }

            FluidStack fluid = FluidStack.CODEC
                    .parse(
                            accessor.getLevel().registryAccess()
                                    .createSerializationContext(NbtOps.INSTANCE),
                            data.getCompound(KEY_FLUID).orElse(new CompoundTag()))
                    .result()
                    .orElse(FluidStack.empty());
            if (fluid.isEmpty()) return;

            long amount   = data.getLongOr(KEY_AMOUNT,   0L);
            long capacity = data.getLongOr(KEY_CAPACITY, 0L);
            tooltip.add(Component.translatable(
                    "jade.mobfarmingsupplies.tank.contents",
                    amount,
                    capacity,
                    fluid.getName()));
        }

        @Override
        public Identifier getUid() {
            return TANK_PROVIDER;
        }
    }
}
