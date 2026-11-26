package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** 1.20.1 loot functions read from JSON through Gson serializers; this makes one from two lambdas. */
public final class LootFunctions {

    private LootFunctions() {}

    public static <T extends LootItemConditionalFunction> LootItemConditionalFunction.Serializer<T> serializer(
            BiFunction<JsonObject, LootItemCondition[], T> reader, BiConsumer<JsonObject, T> writer) {
        return new LootItemConditionalFunction.Serializer<>() {
            @Override
            public void serialize(JsonObject json, T function, JsonSerializationContext context) {
                super.serialize(json, function, context);
                writer.accept(json, function);
            }

            @Override
            public T deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                return reader.apply(json, conditions);
            }
        };
    }
}
