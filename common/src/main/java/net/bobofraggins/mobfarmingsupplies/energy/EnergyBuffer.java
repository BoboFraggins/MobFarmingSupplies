package net.bobofraggins.mobfarmingsupplies.energy;

import org.jetbrains.annotations.Nullable;

/**
 * A block entity's energy store, as the platforms' energy APIs see it (FE on NeoForge, Team Reborn
 * Energy on Fabric): how much it holds, how much fits, and how much neighbours may put in or take
 * out at once. The platform handlers handle transactions, so callers only read and set the amount.
 */
public interface EnergyBuffer {

    long energy();

    long capacity();

    /** Sets the amount; for the platform handlers. */
    void setEnergy(long energy);

    /** Most a neighbour can insert in one go; 0 for a generator. */
    long maxInsert();

    /** Most a neighbour can extract in one go; 0 for a machine. */
    long maxExtract();

    /** The platform handler made for this buffer, kept so every caller shares its transaction state. */
    @Nullable Object energyHandler();

    void setEnergyHandler(Object handler);
}
