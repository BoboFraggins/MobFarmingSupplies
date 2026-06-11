package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Platform bridge for {@link TankBlockEntity} operations that use loader-specific APIs.
 *
 * <p>NeoForge implementation: {@code TankBlockEntityPlatformImpl} (uses NeoForge Transfer API).
 * Fabric implementation: {@code TankBlockEntityPlatformImpl} (uses Fabric Transfer API, Phase 7b).
 */
public final class TankBlockEntityPlatform {

    private TankBlockEntityPlatform() {}

    /**
     * Attempts to transfer fluid between a container item and the tank using the
     * platform's fluid item capability system.
     *
     * <p>If the input item contains fluid: drains it into the tank (must match locked type).
     * If the input item is empty (or a fluid container): fills it from the tank.
     *
     * @param be    the tank block entity
     * @param input a copy of the input item to operate on
     * @return the resulting item after the transfer, or {@code null} if no transfer occurred
     */
    @ExpectPlatform
    @Nullable
    public static ItemStack tryTransferFluidWithItem(TankBlockEntity be, ItemStack input) {
        throw new AssertionError("Missing platform implementation");
    }

    /**
     * Notifies the platform's capability system that this block's capabilities may have changed.
     * No-op on platforms that don't use capability invalidation.
     */
    @ExpectPlatform
    public static void onCapabilitiesChanged(Level level, BlockPos pos) {
        throw new AssertionError("Missing platform implementation");
    }

    /**
     * Whether {@code stack} exposes the platform's fluid-item storage/capability —
     * used by {@link TankMenu} to restrict its fluid-input slot.
     */
    @ExpectPlatform
    public static boolean isFluidContainer(ItemStack stack) {
        throw new AssertionError("Missing platform implementation");
    }
}
