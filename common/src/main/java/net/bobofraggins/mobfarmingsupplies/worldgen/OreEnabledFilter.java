package net.bobofraggins.mobfarmingsupplies.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Placement steps that let one of the mod's ores place only while its config setting is on:
 * {@code mobfarmingsupplies:tritanium_ore_enabled} ({@code tritanium.oreGenerates}) and
 * {@code mobfarmingsupplies:dilithium_ore_enabled} ({@code dilithium.oreGenerates}). Checked as
 * chunks generate, so changing a setting affects only chunks made afterwards.
 */
public class OreEnabledFilter extends PlacementFilter {

    public static final OreEnabledFilter TRITANIUM =
            new OreEnabledFilter(MFSConfig::getTritaniumOreGenerates, () -> Registration.TRITANIUM_ORE_ENABLED.get());
    public static final OreEnabledFilter DILITHIUM =
            new OreEnabledFilter(MFSConfig::getDilithiumOreGenerates, () -> Registration.DILITHIUM_ORE_ENABLED.get());

    public static final MapCodec<OreEnabledFilter> TRITANIUM_CODEC = MapCodec.unit(TRITANIUM);
    public static final MapCodec<OreEnabledFilter> DILITHIUM_CODEC = MapCodec.unit(DILITHIUM);

    private final BooleanSupplier enabled;
    private final Supplier<PlacementModifierType<?>> type;

    private OreEnabledFilter(BooleanSupplier enabled, Supplier<PlacementModifierType<?>> type) {
        this.enabled = enabled;
        this.type = type;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return enabled.getAsBoolean();
    }

    @Override
    public PlacementModifierType<?> type() {
        return type.get();
    }
}
