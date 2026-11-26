package net.bobofraggins.mobfarmingsupplies.forge.register;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.crafting.FluidContainerIngredient;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.forge.LogisticSorterItemHandler;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.forge.incubator.IncubatorItemHandler;
import net.bobofraggins.mobfarmingsupplies.forge.absorptionhopper.AbsorptionHopperFluidHandler;
import net.bobofraggins.mobfarmingsupplies.forge.absorptionhopper.AbsorptionHopperItemHandler;
import net.bobofraggins.mobfarmingsupplies.forge.experiencesyringe.ExperienceSyringeFluidHandler;
import net.bobofraggins.mobfarmingsupplies.forge.loot.DnaSamplePackChestLootModifier;
import net.bobofraggins.mobfarmingsupplies.forge.loot.MagicHatChestLootModifier;
import net.bobofraggins.mobfarmingsupplies.forge.loot.SmoreChestLootModifier;
import net.bobofraggins.mobfarmingsupplies.forge.loot.ToggleButtonChestLootModifier;
import net.bobofraggins.mobfarmingsupplies.forge.tank.InOutSlotsItemHandler;
import net.bobofraggins.mobfarmingsupplies.forge.tank.InsertOnlyTankFluidHandler;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilBlockEntity;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilMenu;
import net.bobofraggins.mobfarmingsupplies.forge.tank.TankFluidHandler;
import net.bobofraggins.mobfarmingsupplies.forge.tank.TankItemFluidHandler;
import net.bobofraggins.mobfarmingsupplies.forge.toilet.ToiletFluidHandler;
import net.bobofraggins.mobfarmingsupplies.forge.toilet.ToiletItemHandler;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.Capabilities;
import net.bobofraggins.mobfarmingsupplies.forge.xpjuice.XpJuiceFluid;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.omnihopper.forge.OmniHopperCapabilities;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.RegisterCapabilitiesEvent;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeOnlyRegistration {

    private ForgeOnlyRegistration() {}

    // ── NeoForge-only DeferredRegisters ──────────────────────────────────────────

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MobFarmingSuppliesCommon.MODID);

    // Note: FLUIDS DeferredRegister removed — fluid registration moved to common Registration (Phase 7c)

    public static final DeferredRegister<com.mojang.serialization.Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MobFarmingSuppliesCommon.MODID);



    // ── Loot modifier ─────────────────────────────────────────────────────────────

    public static final net.minecraftforge.registries.RegistryObject<com.mojang.serialization.Codec<DnaSamplePackChestLootModifier>> DNA_SAMPLE_PACK_CHEST_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("dna_sample_pack_chest",
                    () -> DnaSamplePackChestLootModifier.CODEC);

    public static final net.minecraftforge.registries.RegistryObject<com.mojang.serialization.Codec<ToggleButtonChestLootModifier>> TOGGLE_BUTTON_CHEST_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("toggle_button_chest",
                    () -> ToggleButtonChestLootModifier.CODEC);

    public static final net.minecraftforge.registries.RegistryObject<com.mojang.serialization.Codec<MagicHatChestLootModifier>> MAGIC_HAT_CHEST_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("magic_hat_chest",
                    () -> MagicHatChestLootModifier.CODEC);

    public static final net.minecraftforge.registries.RegistryObject<com.mojang.serialization.Codec<net.bobofraggins.mobfarmingsupplies.forge.loot.AddTableLootModifier>> ADD_TABLE_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("add_table", () -> net.bobofraggins.mobfarmingsupplies.forge.loot.AddTableLootModifier.CODEC);

    public static final net.minecraftforge.registries.RegistryObject<com.mojang.serialization.Codec<SmoreChestLootModifier>> SMORE_CHEST_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("smore_chest",
                    () -> SmoreChestLootModifier.CODEC);

    // ── XP Juice fluid ────────────────────────────────────────────────────────────

    public static final net.minecraftforge.registries.RegistryObject<FluidType> XP_JUICE_TYPE =
            FLUID_TYPES.register("xp_juice",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .density(900).viscosity(1500).temperature(300)
                                    .lightLevel(10)
                                    .sound(SoundActions.BUCKET_FILL,  SoundEvents.PLAYER_LEVELUP)
                                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.EXPERIENCE_ORB_PICKUP)) {
                        @Override
                        public void initializeClient(java.util.function.Consumer<
                                net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                            consumer.accept(net.bobofraggins.mobfarmingsupplies.forge.xpjuice.XpJuiceClientEvents.xpJuice());
                        }
                    });

    // XP_JUICE_SOURCE, XP_JUICE_FLOWING, XP_JUICE_BLOCK, XP_JUICE_BUCKET moved to
    // common Registration (Phase 7c) — use Registration.XP_JUICE_* everywhere.

    public static final ForgeFlowingFluid.Properties XP_JUICE_FLUID_PROPS =
            new ForgeFlowingFluid.Properties(
                    XP_JUICE_TYPE,
                    () -> (XpJuiceFluid.Source) Registration.XP_JUICE_SOURCE.get(),
                    () -> (XpJuiceFluid.Flowing) Registration.XP_JUICE_FLOWING.get())
                    .bucket(() -> Registration.XP_JUICE_BUCKET.get())
                    .block(() -> Registration.XP_JUICE_BLOCK.get())
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(1)
                    .tickRate(20);

    // ── Essences ──────────────────────────────────────────────────────────────────

    public static final java.util.Map<Essence, net.minecraftforge.registries.RegistryObject<FluidType>> ESSENCE_TYPES =
            new java.util.EnumMap<>(Essence.class);
    public static final java.util.Map<Essence, ForgeFlowingFluid.Properties> ESSENCE_FLUID_PROPS =
            new java.util.EnumMap<>(Essence.class);

    static {
        for (Essence essence : Essence.values()) {
            net.minecraftforge.registries.RegistryObject<FluidType> type = FLUID_TYPES.register(essence.fluidId(),
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .density(3000).viscosity(6000).temperature(300)
                                    .lightLevel(15)
                                    .sound(SoundActions.BUCKET_FILL,  SoundEvents.BUCKET_FILL_LAVA)
                                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)) {
                        @Override
                        public void initializeClient(java.util.function.Consumer<
                                net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                            consumer.accept(net.bobofraggins.mobfarmingsupplies.forge.xpjuice.XpJuiceClientEvents.essence(essence));
                        }
                    });
            ESSENCE_TYPES.put(essence, type);
            // Lava's flow. New sources need FluidType's canConvertToSource, which stays off.
            ESSENCE_FLUID_PROPS.put(essence, new ForgeFlowingFluid.Properties(
                    type,
                    () -> Registration.ESSENCE_SOURCES.get(essence).get(),
                    () -> Registration.ESSENCE_FLOWING.get(essence).get())
                    .bucket(() -> Registration.ESSENCE_BUCKETS.get(essence).get())
                    .block(() -> Registration.ESSENCE_BLOCKS.get(essence).get())
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2)
                    .tickRate(30));
        }
    }

    // ── Tank ──────────────────────────────────────────────────────────────────────
    // TANK, TANK_ITEM, TANK_BE_TYPE, TANK_MENU moved to common Registration
    // Note: TANK_CONTENTS also moved to common Registration

    // mob_harvester BE type is now registered in Registration.MOB_HARVESTER_BE_TYPE (common/)

    // ── Bootstrap ─────────────────────────────────────────────────────────────────

    /**
     * Registers all NeoForge-specific entries (adds to common DeferredRegisters,
     * subscribes NeoForge-only DeferredRegisters to the event bus, wires
     * capabilities), then calls {@link Registration#register()} to submit everything.
     */
    public static void register(IEventBus modEventBus) {
        ModCompatRegistration.register();

        net.minecraftforge.common.crafting.CraftingHelper.register(FluidContainerIngredient.ID, FluidContainerIngredient.SERIALIZER);

        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        // FLUIDS removed — fluid instances now in common Registration (Phase 7c)
        // CREATIVE_TABS removed — creative tab moved to common Registration

        // Submit all entries (common + the NeoForge-only ones added above) to the registry
        Registration.register();

        // Forge 1.20.1 has no capability registry: handlers are attached to block entities and stacks.
        modEventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) ->
                registerCapabilities(new RegisterCapabilitiesEvent()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.<net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.level.block.entity.BlockEntity>, net.minecraft.world.level.block.entity.BlockEntity>addGenericListener(
                net.minecraft.world.level.block.entity.BlockEntity.class,
                net.bobofraggins.mobfarmingsupplies.forge.transfer.api.BlockCapability::attach);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.<net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.item.ItemStack>, net.minecraft.world.item.ItemStack>addGenericListener(
                net.minecraft.world.item.ItemStack.class, RegisterCapabilitiesEvent::attachItem);
    }

    // ── Capabilities ─────────────────────────────────────────────────────────────

    // NeoForge capability registration — Fabric equivalents registered in MobFarmingSuppliesFabric.
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Capabilities.Fluid.BLOCK.registerBlockEntity(
                event,
                Registration.TANK_BE_TYPE.get(),
                (be, side) -> new TankFluidHandler(be));

        // The Experience Pylon, at either half.
        Capabilities.Fluid.BLOCK.registerBlock(event, (level, pos, state, be, side) -> {
            var pylon = net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlock.blockEntity(level, pos, state);
            return pylon == null ? null : new TankFluidHandler(pylon);
        }, Registration.EXPERIENCE_PYLON.get());

        event.registerItem(
                net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM,
                (stack, ctx) -> new TankItemFluidHandler(stack),
                Registration.TANK_ITEM.get(), Registration.GOLD_TANK_ITEM.get(),
                Registration.DIAMOND_TANK_ITEM.get(), Registration.EMERALD_TANK_ITEM.get());

        event.registerItem(
                net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM,
                (stack, ctx) -> new ExperienceSyringeFluidHandler(stack),
                Registration.EXPERIENCE_SYRINGE.get());

        Capabilities.Item.BLOCK.registerBlockEntity(
                event,
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                (be, side) -> new AbsorptionHopperItemHandler(be));

        Capabilities.Fluid.BLOCK.registerBlockEntity(
                event,
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                (be, side) -> new AbsorptionHopperFluidHandler(be));

        // Logistic Sorter: insert-only, and only on INPUT sides.
        Capabilities.Item.BLOCK.registerBlockEntity(
                event,
                Registration.LOGISTIC_SORTER_BE_TYPE.get(),
                (be, side) -> side != null && be.getSide(side) == SideMode.INPUT
                        ? new LogisticSorterItemHandler(be) : null);

        // Omnidirectional Hopper: insert-only items/fluids/energy/chemicals, only on INPUT sides.
        OmniHopperCapabilities.register(event);

        // XP Powered Incubator: eggs and DNA in (eggs spread evenly), finished eggs out, on every side.
        Capabilities.Item.BLOCK.registerBlockEntity(event, Registration.INCUBATOR_BE_TYPE.get(),
                (be, side) -> new IncubatorItemHandler(be));

        // Its experience tank: experience in only.
        Capabilities.Fluid.BLOCK.registerBlockEntity(event, Registration.INCUBATOR_BE_TYPE.get(),
                (be, side) -> new InsertOnlyTankFluidHandler(be));

        // Tanks and the Experience Pylon (at either half): containers into the fill-and-empty slot,
        // results out of the other.
        Capabilities.Item.BLOCK.registerBlockEntity(event, Registration.TANK_BE_TYPE.get(),
                (be, side) -> new InOutSlotsItemHandler(be.transferContainer, 0, 1, 64,
                        net.bobofraggins.mobfarmingsupplies.tank.TankMenu::canFillOrEmpty));
        Capabilities.Item.BLOCK.registerBlock(event, (level, pos, state, be, side) -> {
            var pylon = net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlock.blockEntity(level, pos, state);
            return pylon == null ? null : new InOutSlotsItemHandler(pylon.transferContainer, 0, 1, 64,
                    net.bobofraggins.mobfarmingsupplies.tank.TankMenu::canFillOrEmpty);
        }, Registration.EXPERIENCE_PYLON.get());

        // XP Powered Anvil: damaged items in, one at a time; repaired ones out.
        Capabilities.Item.BLOCK.registerBlockEntity(event, Registration.XP_ANVIL_BE_TYPE.get(),
                (be, side) -> new InOutSlotsItemHandler(be.items(), XpAnvilBlockEntity.INPUT, XpAnvilBlockEntity.OUTPUT, 1,
                        XpAnvilMenu::isRepairable));

        // XP Powered Disenchanter: enchanted items and books in, enchanted books (and finished items) out.
        Capabilities.Item.BLOCK.registerBlockEntity(event, Registration.DISENCHANTER_BE_TYPE.get(),
                (be, side) -> new InOutSlotsItemHandler(be.items(), java.util.List.of(
                        new InOutSlotsItemHandler.Input(DisenchanterBlockEntity.ITEM, 1, DisenchanterBlockEntity::isDisenchantable),
                        new InOutSlotsItemHandler.Input(DisenchanterBlockEntity.BOOK, 64, DisenchanterBlockEntity::isBook)),
                        DisenchanterBlockEntity.OUTPUT));
        Capabilities.Fluid.BLOCK.registerBlockEntity(event, Registration.DISENCHANTER_BE_TYPE.get(),
                (be, side) -> new InsertOnlyTankFluidHandler(be));

        // Color Workstation, at either half: Essence of Color in only; dyes out only.
        Capabilities.Fluid.BLOCK.registerBlock(event, (level, pos, state, be, side) -> {
            var station = net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlock.blockEntity(level, pos, state);
            return station == null ? null : new InsertOnlyTankFluidHandler(station);
        }, Registration.COLOR_WORKSTATION.get());
        Capabilities.Item.BLOCK.registerBlock(event, (level, pos, state, be, side) -> {
            var station = net.bobofraggins.mobfarmingsupplies.colorworkstation.ColorWorkstationBlock.blockEntity(level, pos, state);
            return station == null ? null : new net.bobofraggins.mobfarmingsupplies.forge.tank.ColorWorkstationDyeHandler(station);
        }, Registration.COLOR_WORKSTATION.get());

        // XP Powered Anvil: experience in only.
        Capabilities.Fluid.BLOCK.registerBlockEntity(event, Registration.XP_ANVIL_BE_TYPE.get(),
                (be, side) -> new InsertOnlyTankFluidHandler(be));

        // Toilet: voids any item pushed in and supplies unlimited water, on every side.
        Capabilities.Item.BLOCK.registerBlockEntity(
                event,
                Registration.TOILET_BE_TYPE.get(),
                (be, side) -> new ToiletItemHandler(be.getLevel(), be.getBlockPos()));

        Capabilities.Fluid.BLOCK.registerBlockEntity(
                event,
                Registration.TOILET_BE_TYPE.get(),
                (be, side) -> ToiletFluidHandler.INSTANCE);
    }
}
