package net.bobofraggins.mobfarmingsupplies.tritanium;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Placement step {@code mobfarmingsupplies:tritanium_ore_enabled}: lets the Tritanium Ore
 * feature place only while the {@code tritanium.oreGenerates} setting is on. Checked as chunks
 * generate, so changing the setting affects only chunks made afterwards.
 */
public class TritaniumOreEnabledFilter extends PlacementFilter {

    public static final TritaniumOreEnabledFilter INSTANCE = new TritaniumOreEnabledFilter();
    public static final MapCodec<TritaniumOreEnabledFilter> CODEC = MapCodec.unit(INSTANCE);

    private TritaniumOreEnabledFilter() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return MFSConfig.getTritaniumOreGenerates();
    }

    @Override
    public PlacementModifierType<?> type() {
        return Registration.TRITANIUM_ORE_ENABLED.get();
    }
}
