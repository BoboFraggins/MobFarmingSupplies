package net.bobofraggins.mobfarmingsupplies.shared.render;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Fills {@link ItemStackRenderState}s; mirrors the class of this name in later versions. */
public final class ItemModelResolver {

    public static final ItemModelResolver INSTANCE = new ItemModelResolver();

    private ItemModelResolver() {}

    public void updateForTopItem(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context,
                                 @Nullable Level level, @Nullable LivingEntity owner, int seed) {
        state.set(stack, context, level, owner, seed);
    }
}
