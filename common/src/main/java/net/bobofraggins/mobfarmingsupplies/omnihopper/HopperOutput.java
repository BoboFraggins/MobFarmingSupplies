package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** One OUTPUT side of a hopper node: the neighbour of {@code pos} on {@code side}, in {@code level}. */
public record HopperOutput(Level level, BlockPos pos, Direction side) {}
