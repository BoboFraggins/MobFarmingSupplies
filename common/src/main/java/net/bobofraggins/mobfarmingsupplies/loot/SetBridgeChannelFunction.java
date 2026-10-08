package net.bobofraggins.mobfarmingsupplies.loot;

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

    public static final MapCodec<SetBridgeChannelFunction> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).apply(i, SetBridgeChannelFunction::new));

    private SetBridgeChannelFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.set(Registration.BRIDGE_CHANNEL.get(), EinsteinRosenBridgeBlockEntity.randomChannel(context.getRandom()));
        return stack;
    }

    @Override
    public MapCodec<SetBridgeChannelFunction> codec() {
        return CODEC;
    }
}
