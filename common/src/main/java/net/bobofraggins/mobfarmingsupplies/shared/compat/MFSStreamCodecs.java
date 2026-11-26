package net.bobofraggins.mobfarmingsupplies.shared.compat;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

/** Stream codecs that 1.20.5+ keeps on the classes themselves ({@code MFSStreamCodecs.BLOCK_POS}, ...). */
public final class MFSStreamCodecs {

    private MFSStreamCodecs() {}

    public static final StreamCodec<FriendlyByteBuf, BlockPos> BLOCK_POS =
            StreamCodec.of(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
    public static final StreamCodec<FriendlyByteBuf, GlobalPos> GLOBAL_POS =
            StreamCodec.of(FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos);
    public static final StreamCodec<FriendlyByteBuf, Direction> DIRECTION =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(Direction.class));
    public static final StreamCodec<FriendlyByteBuf, DyeColor> DYE_COLOR =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(DyeColor.class));
    public static final StreamCodec<FriendlyByteBuf, CraftingBookCategory> CRAFTING_BOOK_CATEGORY =
            StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(CraftingBookCategory.class));
    /** Any item stack; 1.20.1's buffer form already allows empty ones. */
    public static final StreamCodec<FriendlyByteBuf, ItemStack> ITEM_STACK =
            StreamCodec.of(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);
    public static final StreamCodec<FriendlyByteBuf, ItemStack> OPTIONAL_ITEM_STACK = ITEM_STACK;
    public static final StreamCodec<FriendlyByteBuf, Ingredient> INGREDIENT =
            StreamCodec.of((buf, i) -> i.toNetwork(buf), Ingredient::fromNetwork);
    public static final StreamCodec<FriendlyByteBuf, FluidStack> FLUID_STACK =
            StreamCodec.of((buf, fs) -> fs.write(buf), FluidStack::read);
}
