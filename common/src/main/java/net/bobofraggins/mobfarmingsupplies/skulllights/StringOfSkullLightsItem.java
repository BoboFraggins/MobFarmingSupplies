package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A String of Skull Lights. Used on the face of a solid block it puts a hook there, and the string
 * trails from it to your hand; used on a second block it hangs the string between the two. Sneak-use
 * in the air to take back a string you've started.
 */
public class StringOfSkullLightsItem extends Item {

    /** Where a string being hung starts: the hook's block, and the face of the wall it sticks out of. */
    public record Start(GlobalPos pos, Direction facing) {
        public static final Codec<Start> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Start::pos),
                Direction.CODEC.fieldOf("facing").forGetter(Start::facing)
        ).apply(i, Start::new));

        public static final StreamCodec<ByteBuf, Start> STREAM_CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, Start::pos,
                Direction.STREAM_CODEC, Start::facing,
                Start::new);
    }

    public StringOfSkullLightsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction facing = context.getClickedFace();
        BlockPos hookPos = context.getClickedPos().relative(facing);
        if (!canPlaceHook(level, hookPos, facing)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        SkullLightHookBlockEntity originHook = pendingHook(level, stack, player);
        if (originHook == null) {
            // Start a string: hang its first hook here, and carry the string on from it.
            stack.remove(Registration.SKULL_LIGHTS_START.get()); // a start that came loose
            level.setBlock(hookPos, hookFacing(facing), Block.UPDATE_ALL);
            if (player != null && level.getBlockEntity(hookPos) instanceof SkullLightHookBlockEntity hook) {
                hook.startHanging(player.getUUID(), lightsOf(stack));
                stack.set(Registration.SKULL_LIGHTS_START.get(), new Start(GlobalPos.of(level.dimension(), hookPos), facing));
                tell(player, Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.started"));
            }
            playSound(level, hookPos, 1.2f);
            return InteractionResult.SUCCESS;
        }

        BlockPos startPos = originHook.getBlockPos();
        Vec3 from = originHook.attachPoint();
        Vec3 to = SkullLightsCurve.attachPoint(hookPos, facing);
        if (from.distanceTo(to) > SkullLightsCurve.MAX_SPAN) {
            tell(player, Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.too_far",
                    (int) SkullLightsCurve.MAX_SPAN));
            return InteractionResult.FAIL;
        }
        level.setBlock(hookPos, hookFacing(facing), Block.UPDATE_ALL);
        if (level.getBlockEntity(hookPos) instanceof SkullLightHookBlockEntity endHook) {
            originHook.hang(hookPos, lightsOf(stack));
            endHook.attachTo(startPos);
        }
        playSound(level, hookPos, 0.9f);
        // A finished string counts as placing a block, as hanging any decoration would.
        if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, hookPos, stack);
        stack.remove(Registration.SKULL_LIGHTS_START.get());
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static BlockState hookFacing(Direction facing) {
        return Registration.SKULL_LIGHT_HOOK.get().defaultBlockState().setValue(SkullLightHookBlock.FACING, facing);
    }

    /** The hook {@code player} started hanging {@code stack} from, if it's still up and waiting. */
    @Nullable
    private static SkullLightHookBlockEntity pendingHook(Level level, ItemStack stack, @Nullable Player player) {
        Start start = stack.get(Registration.SKULL_LIGHTS_START.get());
        if (start == null || player == null || start.pos().dimension() != level.dimension()) return null;
        return level.getBlockEntity(start.pos().pos()) instanceof SkullLightHookBlockEntity hook && hook.awaits(player)
                ? hook : null;
    }

    /** Whether {@code player} still carries the string they started hanging from the hook at {@code hookPos}. */
    static boolean isHanging(Player player, Level level, BlockPos hookPos) {
        return startedStack(player, level, hookPos) != null;
    }

    /** Clears the start from the string {@code player} was hanging from the hook at {@code hookPos}. */
    static void forgetStart(Player player, Level level, BlockPos hookPos) {
        ItemStack stack = startedStack(player, level, hookPos);
        if (stack != null) stack.remove(Registration.SKULL_LIGHTS_START.get());
    }

    @Nullable
    private static ItemStack startedStack(Player player, Level level, BlockPos hookPos) {
        GlobalPos at = GlobalPos.of(level.dimension(), hookPos);
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            Start start = stack.get(Registration.SKULL_LIGHTS_START.get());
            if (start != null && start.pos().equals(at)) return stack;
        }
        ItemStack carried = player.containerMenu.getCarried();
        Start start = carried.get(Registration.SKULL_LIGHTS_START.get());
        return start != null && start.pos().equals(at) ? carried : null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !stack.has(Registration.SKULL_LIGHTS_START.get())) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            SkullLightHookBlockEntity hook = pendingHook(level, stack, player);
            stack.remove(Registration.SKULL_LIGHTS_START.get());
            if (hook != null) level.removeBlock(hook.getBlockPos(), false);
            tell(player, Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.cancelled"));
        }
        return InteractionResult.SUCCESS;
    }

    /** A hook can go at {@code pos} if the space is free and the block behind it has a solid face. */
    private static boolean canPlaceHook(Level level, BlockPos pos, Direction facing) {
        return level.getBlockState(pos).canBeReplaced() && SkullLightHookBlock.canHang(level, pos, facing);
    }

    public static SkullLights lightsOf(ItemStack stack) {
        SkullLights lights = stack.get(Registration.SKULL_LIGHTS.get());
        return lights != null ? lights : SkullLights.defaultLights();
    }

    private static void tell(Player player, Component message) {
        if (player != null) player.sendOverlayMessage(message);
    }

    private static void playSound(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1f, pitch);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(Registration.SKULL_LIGHTS_START.get()) || super.isFoil(stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        SkullLights lights = lightsOf(stack);
        for (ItemStack skull : lights.skulls()) {
            tooltip.accept(skull.getHoverName().copy().withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(lights.lit()
                ? Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.color",
                        Component.translatable("color.minecraft." + lights.color().getName())).withStyle(ChatFormatting.GRAY)
                : Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.unlit").withStyle(ChatFormatting.GRAY));
        if (stack.has(Registration.SKULL_LIGHTS_START.get())) {
            tooltip.accept(Component.translatable("item.mobfarmingsupplies.string_of_skull_lights.hanging")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
