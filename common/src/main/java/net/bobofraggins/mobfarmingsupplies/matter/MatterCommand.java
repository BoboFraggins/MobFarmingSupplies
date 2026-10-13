package net.bobofraggins.mobfarmingsupplies.matter;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/** {@code /mobfarmingsupplies matter <item>}: says what an item is worth in matter. */
public final class MatterCommand {

    private MatterCommand() {}

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, context, selection) -> dispatcher.register(
                Commands.literal("mobfarmingsupplies").then(Commands.literal("matter")
                        .then(Commands.argument("item", ItemArgument.item(context)).executes(command -> {
                            Item item = ItemArgument.getItem(command, "item").item().value();
                            int value = MatterValues.get(item);
                            command.getSource().sendSuccess(() -> value > 0
                                    ? Component.translatable("commands.mobfarmingsupplies.matter.value",
                                            item.getDefaultInstance().getHoverName(), value)
                                    : Component.translatable("commands.mobfarmingsupplies.matter.none",
                                            item.getDefaultInstance().getHoverName()), false);
                            return value;
                        })))));
    }
}
