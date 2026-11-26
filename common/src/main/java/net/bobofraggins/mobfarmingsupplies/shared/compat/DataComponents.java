package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

/**
 * The vanilla 1.20.5+ item components this mod reads or writes, mapped onto the item NBT keys that
 * 1.20.1 uses for the same thing.
 */
public final class DataComponents {

    private DataComponents() {}

    /** A player head's owner: {@code SkullOwner}. */
    public static final DataComponentType<GameProfile> PROFILE = DataComponentType.custom("SkullOwner",
            tag -> tag instanceof CompoundTag c ? NbtUtils.readGameProfile(c) : null,
            profile -> NbtUtils.writeGameProfile(new CompoundTag(), profile));

    /** A block item's block entity data: {@code BlockEntityTag}, loaded by vanilla when the block is placed. */
    public static final DataComponentType<CustomData> BLOCK_ENTITY_DATA = DataComponentType.custom("BlockEntityTag",
            DataComponents::compound, data -> data.copyTag());

    /** Free-form data of this mod's own. */
    public static final DataComponentType<CustomData> CUSTOM_DATA = DataComponentType.custom("mobfarmingsupplies:custom_data",
            DataComponents::compound, data -> data.copyTag());

    private static CustomData compound(Tag tag) {
        return tag instanceof CompoundTag c ? CustomData.of(c) : null;
    }
}
