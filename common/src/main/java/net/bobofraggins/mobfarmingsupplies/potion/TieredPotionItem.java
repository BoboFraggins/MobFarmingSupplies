package net.bobofraggins.mobfarmingsupplies.potion;

import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.oresight.Oresight;
import net.bobofraggins.mobfarmingsupplies.oresight.OresightTargets;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * A Lesser, Greater or Superior Potion of Flight or of Oresight, brewed in a brewing stand
 * ({@link MFSBrewing}). Drunk like a vanilla potion, it gives its effect for its tier's duration
 * and leaves a glass bottle (the {@code CONSUMABLE} component set where it's registered).
 * Oresight potions carry the ore they find as the {@code oresight_block} component.
 *
 * <p>They're items of their own rather than vanilla potions, so gunpowder and dragon's breath
 * don't turn them into splash or lingering potions.
 */
public class TieredPotionItem extends Item {

    public enum Kind {
        FLIGHT("flight"),
        ORESIGHT("oresight");

        private final String id;

        Kind(String id) { this.id = id; }

        public String id() { return id; }
    }

    private final Kind kind;
    private final PotionTier tier;

    public TieredPotionItem(Kind kind, PotionTier tier, Properties properties) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
    }

    public Kind kind() { return kind; }

    public PotionTier tier() { return tier; }

    public static ItemStack flight(PotionTier tier) {
        return new ItemStack(Registration.FLIGHT_POTIONS.get(tier).get());
    }

    /** A Potion of Oresight of {@code tier} for {@code ore}. */
    public static ItemStack oresight(PotionTier tier, Block ore) {
        ItemStack stack = new ItemStack(Registration.ORESIGHT_POTIONS.get(tier).get());
        stack.set(Registration.ORESIGHT_BLOCK.get(), ore);
        return stack;
    }

    /** {@code stack}, brewed up to {@code next}: the same kind of potion, for the same ore. */
    public ItemStack withTier(ItemStack stack, PotionTier next) {
        return switch (kind) {
            case FLIGHT -> flight(next);
            case ORESIGHT -> {
                Block ore = stack.get(Registration.ORESIGHT_BLOCK.get());
                yield ore == null ? ItemStack.EMPTY : oresight(next, ore);
            }
        };
    }

    private Holder<MobEffect> effect() {
        return switch (kind) {
            case FLIGHT -> BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registration.FLIGHT.get());
            case ORESIGHT -> Oresight.effect();
        };
    }

    @Nullable
    private Block ore(ItemStack stack) {
        return stack.get(Registration.ORESIGHT_BLOCK.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        if (kind == Kind.FLIGHT) return super.getName(stack);
        Block ore = ore(stack);
        return ore == null
                ? Component.translatable(getDescriptionId() + ".unknown")
                : Component.translatable(getDescriptionId(), OresightTargets.name(ore));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // An Oresight potion with no ore (only from commands) has nothing to show.
        if (kind == Kind.ORESIGHT && ore(player.getItemInHand(hand)) == null) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            Block ore = ore(stack);
            if (kind == Kind.FLIGHT) {
                entity.addEffect(new MobEffectInstance(effect(), tier.durationTicks()));
            } else if (ore != null && entity instanceof ServerPlayer player) {
                player.addEffect(new MobEffectInstance(effect(), tier.durationTicks()));
                Oresight.setTarget(player, ore);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    /** "Flight (03:00)" in blue, as a vanilla potion shows its effect. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (kind == Kind.ORESIGHT && ore(stack) == null) return;
        tooltip.accept(Component.translatable("potion.withDuration",
                        Component.translatable(effect().value().getDescriptionId()),
                        StringUtil.formatTickDuration(tier.durationTicks(), 20f))
                .withStyle(ChatFormatting.BLUE));
    }
}
