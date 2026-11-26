package net.bobofraggins.mobfarmingsupplies.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

/**
 * Lazy registry lookups that break circular class-initialization dependencies between
 * {@link Registration} and block-entity / item classes.
 *
 * <p>Using {@link Registration#TANK_BE_TYPE}{@code .get()} directly inside a block-entity
 * constructor causes javac to complain because the static initialiser of
 * {@code Registration} hasn't finished by the time the block entity class is loaded.
 * Looking up the type by name from {@link BuiltInRegistries} defers the resolution to
 * runtime, after all static initialisers have completed.
 */
public final class MFSRegistryHelper {

    private static final String MODID = "mobfarmingsupplies";

    private MFSRegistryHelper() {}

    /** Returns the registered {@link BlockEntityType} for the given registry name. */
    public static BlockEntityType<?> getBEType(String key) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.get(
                new ResourceLocation(MODID, key));
    }

    /** Returns the registered {@link Item} for the given registry name. */
    public static Item getItem(String key) {
        return BuiltInRegistries.ITEM.get(
                new ResourceLocation(MODID, key));
    }

    /** Returns the registered {@link Fluid} for the given registry name. */
    public static Fluid getFluid(String key) {
        return BuiltInRegistries.FLUID.get(
                new ResourceLocation(MODID, key));
    }
}
