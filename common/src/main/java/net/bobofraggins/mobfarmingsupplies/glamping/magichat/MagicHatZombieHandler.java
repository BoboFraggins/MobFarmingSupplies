package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Gives a random 5% of naturally-spawning zombies a Magic Hat containing a surprise mob.
 *
 * <p>Platform-specific spawn hooks ({@code FinalizeSpawnEvent} on NeoForge, a
 * {@code Mob#finalizeSpawn} mixin on Fabric) filter for genuinely-natural spawns and call
 * {@link #tryEquipMagicHat}.
 */
public final class MagicHatZombieHandler {

    private MagicHatZombieHandler() {}

    public static void tryEquipMagicHat(Zombie zombie, RandomSource random) {
        if (random.nextFloat() >= 0.05f) return;

        EntityType<?> mobType = pickMobType(random);
        Level level = zombie.level();
        Entity mob = mobType.create(level, EntitySpawnReason.TRIGGERED);
        if (mob == null) return;

        TagValueOutput tagOut = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        if (!mob.save(tagOut)) return;
        CompoundTag mobTag = tagOut.buildResult();

        CompoundTag wrapper = new CompoundTag();
        wrapper.put(MagicHatItem.MOB_KEY, mobTag);

        ItemStack hat = new ItemStack(Registration.MAGIC_HAT_ITEM.get());
        hat.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));

        zombie.setItemSlot(EquipmentSlot.HEAD, hat);
        zombie.setDropChance(EquipmentSlot.HEAD, 1.0f);
    }

    private static EntityType<?> pickMobType(RandomSource random) {
        float r = random.nextFloat();
        if (r < 0.50f) return EntityTypes.RABBIT;
        if (r < 0.75f) return EntityTypes.CHICKEN;
        if (r < 0.80f) return EntityTypes.SHEEP;
        if (r < 0.85f) return EntityTypes.COW;
        if (r < 0.90f) return EntityTypes.CAT;
        if (r < 0.95f) return EntityTypes.VILLAGER;
        return EntityTypes.RAVAGER;
    }
}
