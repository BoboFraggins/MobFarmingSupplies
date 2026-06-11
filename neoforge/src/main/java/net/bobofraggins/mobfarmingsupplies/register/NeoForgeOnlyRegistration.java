package net.bobofraggins.mobfarmingsupplies.register;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperFluidHandler;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperItemHandler;
import net.bobofraggins.mobfarmingsupplies.crafting.FluidContainerIngredient;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeFluidHandler;
import net.bobofraggins.mobfarmingsupplies.loot.DnaSamplePackChestLootModifier;
import net.bobofraggins.mobfarmingsupplies.tank.TankFluidHandler;
import net.bobofraggins.mobfarmingsupplies.tank.TankItemFluidHandler;
import net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuiceFluid;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NeoForgeOnlyRegistration {

    private NeoForgeOnlyRegistration() {}

    // ── NeoForge-only DeferredRegisters ──────────────────────────────────────────

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MobFarmingSuppliesCommon.MODID);

    // Note: FLUIDS DeferredRegister removed — fluid registration moved to common Registration (Phase 7c)

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MobFarmingSuppliesCommon.MODID);

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, MobFarmingSuppliesCommon.MODID);


    // ── Fluid ingredient type ─────────────────────────────────────────────────────

    public static final net.neoforged.neoforge.registries.DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>>
            FLUID_INGREDIENT_TYPE = INGREDIENT_TYPES.register("fluid_container",
                    () -> new IngredientType<>(FluidContainerIngredient.CODEC));

    // ── Loot modifier ─────────────────────────────────────────────────────────────

    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>,
            MapCodec<DnaSamplePackChestLootModifier>> DNA_SAMPLE_PACK_CHEST_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("dna_sample_pack_chest",
                    () -> DnaSamplePackChestLootModifier.CODEC);

    // ── XP Juice fluid ────────────────────────────────────────────────────────────

    public static final net.neoforged.neoforge.registries.DeferredHolder<FluidType, FluidType> XP_JUICE_TYPE =
            FLUID_TYPES.register("xp_juice",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .density(900).viscosity(1500).temperature(300)
                                    .lightLevel(10)
                                    .sound(SoundActions.BUCKET_FILL,  SoundEvents.PLAYER_LEVELUP)
                                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.EXPERIENCE_ORB_PICKUP)));

    // XP_JUICE_SOURCE, XP_JUICE_FLOWING, XP_JUICE_BLOCK, XP_JUICE_BUCKET moved to
    // common Registration (Phase 7c) — use Registration.XP_JUICE_* everywhere.

    public static final BaseFlowingFluid.Properties XP_JUICE_FLUID_PROPS =
            new BaseFlowingFluid.Properties(
                    XP_JUICE_TYPE,
                    () -> (XpJuiceFluid.Source) Registration.XP_JUICE_SOURCE.get(),
                    () -> (XpJuiceFluid.Flowing) Registration.XP_JUICE_FLOWING.get())
                    .bucket(() -> Registration.XP_JUICE_BUCKET.get())
                    .block(() -> Registration.XP_JUICE_BLOCK.get())
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(1)
                    .tickRate(20);

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

        FluidContainerIngredient.typeHolder = FLUID_INGREDIENT_TYPE;

        INGREDIENT_TYPES.register(modEventBus);
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        // FLUIDS removed — fluid instances now in common Registration (Phase 7c)
        // CREATIVE_TABS removed — creative tab moved to common Registration

        // Submit all entries (common + the NeoForge-only ones added above) to the registry
        Registration.register();

        modEventBus.addListener(NeoForgeOnlyRegistration::registerCapabilities);
    }

    // ── Capabilities ─────────────────────────────────────────────────────────────

    // NeoForge capability registration — Fabric equivalents registered in MobFarmingSuppliesFabric.
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                Registration.TANK_BE_TYPE.get(),
                (be, side) -> new TankFluidHandler(be));

        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, ctx) -> new TankItemFluidHandler(stack),
                Registration.TANK_ITEM.get());

        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, ctx) -> ctx != null ? new ExperienceSyringeFluidHandler(ctx) : null,
                Registration.EXPERIENCE_SYRINGE.get());

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                (be, side) -> new AbsorptionHopperItemHandler(be));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                (be, side) -> new AbsorptionHopperFluidHandler(be));
    }
}
