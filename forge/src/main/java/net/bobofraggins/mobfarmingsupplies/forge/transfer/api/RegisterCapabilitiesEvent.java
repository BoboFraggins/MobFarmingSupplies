package net.bobofraggins.mobfarmingsupplies.forge.transfer.api;

import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stands in for NeoForge's capability registration event: block handlers go to
 * {@link BlockCapability#registerBlockEntity}/{@link BlockCapability#registerBlock}, item handlers
 * to {@link #registerItem}, and both are attached through Forge's {@link AttachCapabilitiesEvent}.
 */
public final class RegisterCapabilitiesEvent {

    private static final java.util.Map<Item, java.util.List<ItemEntry<?>>> ITEMS = new java.util.IdentityHashMap<>();

    private record ItemEntry<T>(Capability<T> capability, Function<ItemStack, T> factory) {}

    @FunctionalInterface
    public interface ItemCapabilityProvider<T> {
        @Nullable
        T getCapability(ItemStack stack, @Nullable Void context);
    }

    /** Gives every stack of {@code items} a handler for {@code capability}, made by {@code provider}. */
    public <T> void registerItem(Capability<T> capability, ItemCapabilityProvider<? extends T> provider, Item... items) {
        for (Item item : items) {
            ITEMS.computeIfAbsent(item, i -> new java.util.ArrayList<>())
                    .add(new ItemEntry<>(capability, stack -> provider.getCapability(stack, null)));
        }
    }

    /** Attaches the registered item handlers; called for {@code AttachCapabilitiesEvent<ItemStack>}. */
    public static void attachItem(AttachCapabilitiesEvent<ItemStack> event) {
        java.util.List<ItemEntry<?>> entries = ITEMS.get(event.getObject().getItem());
        if (entries == null) return;
        ItemStack stack = event.getObject();
        event.addCapability(new net.minecraft.resources.ResourceLocation("mobfarmingsupplies", "item_handlers"),
                new ICapabilityProvider() {
                    private final java.util.Map<Capability<?>, LazyOptional<?>> made = new java.util.HashMap<>();

                    @Override
                    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
                        for (ItemEntry<?> entry : entries) {
                            if (entry.capability() != cap) continue;
                            return made.computeIfAbsent(cap, c -> {
                                Object handler = entry.factory().apply(stack);
                                return handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
                            }).cast();
                        }
                        return LazyOptional.empty();
                    }
                });
    }
}
