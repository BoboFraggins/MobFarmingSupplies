package net.bobofraggins.mobfarmingsupplies.loot;

import net.bobofraggins.mobfarmingsupplies.shared.compat.LootFunctions;
import net.minecraft.util.GsonHelper;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function {@code mobfarmingsupplies:set_bridge_channel}: puts the stack on a new random
 * Einstein-Rosen Bridge channel, so a stack of two is a linked pair.
 */
public class SetBridgeChannelFunction extends LootItemConditionalFunction {

    public static final LootItemConditionalFunction.Serializer<SetBridgeChannelFunction> SERIALIZER =
            LootFunctions.serializer((json, conditions) -> new SetBridgeChannelFunction(conditions), (json, f) -> {});

    private SetBridgeChannelFunction(LootItemCondition[] predicates) {
        super(predicates);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        ItemComponents.set(stack, Registration.BRIDGE_CHANNEL.get(), EinsteinRosenBridgeBlockEntity.randomChannel(context.getRandom()));
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Registration.SET_BRIDGE_CHANNEL.get();
    }
}
