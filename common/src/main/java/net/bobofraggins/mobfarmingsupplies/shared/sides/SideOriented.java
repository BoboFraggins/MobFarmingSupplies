package net.bobofraggins.mobfarmingsupplies.shared.sides;

import net.minecraft.core.FrontAndTop;
import org.jetbrains.annotations.Nullable;

/** A block entity whose side-configuration grid follows how it was placed (see {@link SideLayout}). */
public interface SideOriented {

    /** Null for blocks placed before orientations existed: they keep their legacy grid. */
    @Nullable FrontAndTop getSideOrientation();

    void setSideOrientation(FrontAndTop orientation);
}
