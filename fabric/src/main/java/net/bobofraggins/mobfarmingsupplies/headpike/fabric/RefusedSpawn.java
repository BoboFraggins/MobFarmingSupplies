package net.bobofraggins.mobfarmingsupplies.headpike.fabric;

/**
 * A mob a Head Pike has refused (Fabric). It's marked when it's set up for spawning, and kept out
 * of the world when it's added: the way NeoForge cancels a spawn.
 */
public interface RefusedSpawn {

    boolean mobfarmingsupplies$isRefused();
}
