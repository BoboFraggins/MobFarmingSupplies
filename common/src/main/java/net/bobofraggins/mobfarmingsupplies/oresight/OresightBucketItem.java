package net.bobofraggins.mobfarmingsupplies.oresight;

import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.essencehopper.EssenceBucketItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Bucket of Oresight (ore): a Bucket of Sight Essence crafted with an ore ({@link OresightRecipe}).
 * Drunk like a Milk Bucket, it gives Oresight for that ore and its variants for 30 minutes and leaves an empty
 * bucket. The block is the {@code oresight_block} component; drinking and the empty bucket come
 * from the {@code CONSUMABLE} component set where it's registered.
 */
public class OresightBucketItem extends Item {

    public OresightBucketItem(Properties properties) {
        super(properties);
    }

    /** A Bucket of Oresight for {@code block}. */
    public static ItemStack of(Block block) {
        ItemStack stack = new ItemStack(Registration.ORESIGHT_BUCKET.get());
        stack.set(Registration.ORESIGHT_BLOCK.get(), block);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        Block block = stack.get(Registration.ORESIGHT_BLOCK.get());
        return block == null
                ? Component.translatable("item.mobfarmingsupplies.oresight_bucket.unknown")
                : Component.translatable("item.mobfarmingsupplies.oresight_bucket", OresightTargets.name(block));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).has(Registration.ORESIGHT_BLOCK.get())) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Block block = stack.get(Registration.ORESIGHT_BLOCK.get());
        if (block != null && entity instanceof ServerPlayer player) {
            player.addEffect(new MobEffectInstance(Oresight.effect(), EssenceBucketItem.DRINK_DURATION));
            Oresight.setTarget(player, block);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (!stack.has(Registration.ORESIGHT_BLOCK.get())) return;
        tooltip.accept(Component.translatable("item.mobfarmingsupplies.essence_bucket.drink",
                Component.translatable(Registration.ORESIGHT.get().getDescriptionId()), "30:00")
                .withStyle(ChatFormatting.BLUE));
    }
}
