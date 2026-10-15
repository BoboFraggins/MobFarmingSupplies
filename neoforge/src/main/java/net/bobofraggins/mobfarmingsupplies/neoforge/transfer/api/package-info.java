/**
 * A small stand-in for NeoForge's Transfer API (NeoForge 21.9+), which NeoForge 21.1 doesn't have.
 * It has the same names and shapes as the parts this mod uses, so the transfer code is the same as
 * on the 26.x branches apart from imports.
 *
 * <p>Our own handlers take part in transactions exactly, through {@link
 * net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.SnapshotJournal}. Other mods'
 * handlers are 1.21.1 {@code IItemHandler}s, {@code IFluidHandler}s and {@code IEnergyStorage}s,
 * which can't be rolled back: {@link net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Wrappers}
 * simulates them while a transaction is open and makes the moves for real only when the outermost
 * transaction commits. Our handlers are offered to other mods as those 1.21.1 interfaces, each call
 * run in a transaction of its own.
 */
package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;
