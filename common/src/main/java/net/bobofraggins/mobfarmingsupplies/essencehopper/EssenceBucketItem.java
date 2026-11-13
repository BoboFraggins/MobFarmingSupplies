package net.bobofraggins.mobfarmingsupplies.essencehopper;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;

/**
 * A bucket of an essence. Used on a block it places the essence like any bucket. A drinkable one
 * ({@link Essence#drinkable()}) used with no block in reach is drunk instead, like a Milk Bucket,
 * giving {@link Essence#drinkEffect()} for {@link #DRINK_DURATION} ticks and leaving an empty
 * bucket. Drinkable buckets carry the {@link DataComponents#CONSUMABLE} component (set where
 * they're registered), which handles the drinking and the empty bucket.
 */
public class EssenceBucketItem extends BucketItem {

    /** 30 minutes. */
    public static final int DRINK_DURATION = 30 * 60 * 20;

    private final Essence essence;

    public EssenceBucketItem(Essence essence, Fluid fluid, Properties properties) {
        super(fluid, properties);
        this.essence = essence;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Consumable drink = stack.get(DataComponents.CONSUMABLE);
        if (drink != null && getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getType() == HitResult.Type.MISS) {
            return drink.startConsuming(player, stack, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Holder<MobEffect> effect = essence.drinkEffect();
        if (effect != null && !level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(effect, DRINK_DURATION));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Holder<MobEffect> effect = essence.drinkEffect();
        if (effect == null) return;
        tooltip.accept(Component.translatable("item.mobfarmingsupplies.essence_bucket.drink",
                Component.translatable(effect.value().getDescriptionId()), "30:00").withStyle(ChatFormatting.BLUE));
    }
}
