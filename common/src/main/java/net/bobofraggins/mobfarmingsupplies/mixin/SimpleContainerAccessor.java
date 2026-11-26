package net.bobofraggins.mobfarmingsupplies.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 1.20.1's {@code SimpleContainer} keeps its list private (1.20.5+ has {@code getItems()}). */
@Mixin(SimpleContainer.class)
public interface SimpleContainerAccessor {

    @Accessor("items")
    NonNullList<ItemStack> mobfarmingsupplies$items();
}
