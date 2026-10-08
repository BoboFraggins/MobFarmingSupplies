package net.bobofraggins.mobfarmingsupplies.mobhead;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

/** The Mob Head item: named after its mob ("Cow Head"); places a standing or wall head. */
public class MobHeadItem extends StandingAndWallBlockItem {

    public MobHeadItem(Block standing, Block wall, Properties props) {
        super(standing, wall, Direction.DOWN, props);
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = stack.get(Registration.MOB_HEAD_TYPE.get());
        return type == null ? super.getName(stack)
                : Component.translatable("item.mobfarmingsupplies.mob_head.named", type.getDescription());
    }
}
