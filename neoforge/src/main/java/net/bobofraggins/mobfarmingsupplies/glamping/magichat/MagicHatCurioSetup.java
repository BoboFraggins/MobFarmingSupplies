package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Curios registration for the Magic Hat, deliberately isolated in its own class.
 *
 * <p>Classes that are always loaded (like the main {@code @Mod} class, or anything registered
 * wholesale via {@code modEventBus.register(SomeClass.class)}) must never symbolically
 * reference a Curios API type in any of their own method bodies — doing so forces the JVM to
 * resolve Curios' interfaces during <em>bytecode verification</em> of that class, which throws
 * {@link NoClassDefFoundError} the moment the class is loaded, before any {@code isModLoaded}
 * runtime guard or {@code try/catch} inside it ever executes. (This is exactly what broke the
 * mod the first time around — the registration lambda lived directly in
 * {@code MobFarmingSupplies}'s constructor, so the whole mod failed to construct without
 * Curios installed.)
 *
 * <p>The fix: keep every Curios reference in a class that is only ever loaded — and thus only
 * ever verified — <em>after</em> confirming Curios is present. Callers must check
 * {@code ModList.get().isLoaded("curios")} themselves before calling into this class.
 */
public final class MagicHatCurioSetup {

    private MagicHatCurioSetup() {}

    public static void onCommonSetup(IEventBus modEventBus) {
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() ->
                top.theillusivec4.curios.api.CuriosApi.registerCurio(
                        Registration.MAGIC_HAT_ITEM.get(), MagicHatCurioIntegration.INSTANCE)));
    }

    public static void onClientSetup(IEventBus modEventBus) {
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                top.theillusivec4.curios.api.client.ICurioRenderer.register(
                        Registration.MAGIC_HAT_ITEM.get(), MagicHatCurioRenderer::new)));
    }
}
