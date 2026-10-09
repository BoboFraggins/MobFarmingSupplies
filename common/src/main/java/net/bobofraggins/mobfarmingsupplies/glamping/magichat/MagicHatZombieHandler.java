package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.glamping.NaturalSpawnRolls;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
 *
 * <p>Clone-O-Matic spawns use {@link #CLONE_O_MATIC_CHANCE} instead (see {@link NaturalSpawnRolls}),
 * which also covers DNA samples, which never run spawn setup at all.
 */
public final class MagicHatZombieHandler {

    /** Chance for a naturally-spawned zombie. */
    public static final float NATURAL_SPAWN_CHANCE = 0.05f;
    /** Chance for a zombie made by the Clone-O-Matic. */
    public static final float CLONE_O_MATIC_CHANCE = 0.01f;

    private MagicHatZombieHandler() {}

    /** Natural-spawn roll, called from the platform spawn hooks. */
    public static void tryEquipMagicHat(Zombie zombie, RandomSource random) {
        if (NaturalSpawnRolls.suppressed()) return;
        tryEquipMagicHat(zombie, random, NATURAL_SPAWN_CHANCE);
    }

    public static void tryEquipMagicHat(Zombie zombie, RandomSource random, float chance) {
        if (!MFSConfig.getMagicHatZombiesWearHats()) return;
        if (random.nextFloat() >= chance) return;

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
        if (r < 0.50f) return EntityType.RABBIT;
        if (r < 0.75f) return EntityType.CHICKEN;
        if (r < 0.80f) return EntityType.SHEEP;
        if (r < 0.85f) return EntityType.COW;
        if (r < 0.90f) return EntityType.CAT;
        if (r < 0.95f) return EntityType.VILLAGER;
        return EntityType.RAVAGER;
    }
}
