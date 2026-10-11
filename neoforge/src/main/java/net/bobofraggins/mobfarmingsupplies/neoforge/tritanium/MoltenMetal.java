package net.bobofraggins.mobfarmingsupplies.neoforge.tritanium;

import dev.architectury.registry.registries.RegistrySupplier;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.neoforge.register.NeoForgeOnlyRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * A molten metal for Productive Metalworks: its fluid type, source and flowing fluids, block and
 * bucket, all named after {@code name} ({@code molten_<name>}). Its textures are
 * {@code block/molten_<name>_still} and {@code _flow}; see {@link MoltenMetalClientEvents}.
 */
public final class MoltenMetal {

    public final String name;
    public final DeferredHolder<FluidType, FluidType> type;
    public final RegistrySupplier<BaseFlowingFluid.Source> source;
    public final RegistrySupplier<BaseFlowingFluid.Flowing> flowing;
    public final RegistrySupplier<LiquidBlock> block;
    public final RegistrySupplier<BucketItem> bucket;

    private BaseFlowingFluid.Properties properties;

    /** Adds the fluid to the registers. Call before {@link Registration#register()}. */
    public MoltenMetal(String metal) {
        name = "molten_" + metal;
        type = NeoForgeOnlyRegistration.FLUID_TYPES.register(name,
                () -> new FluidType(FluidType.Properties.create()
                        .density(3000).viscosity(6000).temperature(1300).lightLevel(15)));
        source = Registration.FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties));
        flowing = Registration.FLUIDS.register(name + "_flowing", () -> new BaseFlowingFluid.Flowing(properties));
        block = Registration.BLOCKS.register(name,
                () -> new LiquidBlock(source.get(), BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id(name)))
                        .noCollision()
                        .strength(100f)
                        .noLootTable()
                        .liquid()
                        .replaceable()
                        .pushReaction(PushReaction.DESTROY)
                        .lightLevel(state -> 15)));
        bucket = Registration.ITEMS.register(name + "_bucket",
                () -> new BucketItem(source.get(), new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id(name + "_bucket")))
                        .stacksTo(1)
                        .craftRemainder(Items.BUCKET)));
        properties = new BaseFlowingFluid.Properties(type, source, flowing)
                .bucket(bucket)
                .block(block)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .tickRate(20);
    }

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, path);
    }
}
