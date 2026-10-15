package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import com.mojang.logging.LogUtils;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Mekanism 10.7's chemical handlers in this package's shapes, as {@link Wrappers} and {@link Adapters}
 * do for items, fluids and energy. Refers to Mekanism's API, so it is only ever loaded once Mekanism
 * is known to be installed.
 */
public final class MekanismChemicals {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Mekanism's empty chemical. */
    public static final Resource EMPTY = ChemicalResource.EMPTY;

    private MekanismChemicals() {}

    /** Mekanism's chemical block capability, seen through {@link ResourceHandler}s of {@link ChemicalResource}s. */
    public static BlockCapability<ResourceHandler<Resource>, Direction> capability() {
        net.neoforged.neoforge.capabilities.BlockCapability<IChemicalHandler, Direction> raw =
                net.neoforged.neoforge.capabilities.BlockCapability.createSided(
                        ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);
        return new BlockCapability<>(raw, MekanismChemicals::wrap, MekanismChemicals::adapt);
    }

    @Nullable
    private static ResourceHandler<Resource> wrap(@Nullable IChemicalHandler handler) {
        if (handler == null) return null;
        if (handler instanceof ChemicalAdapter adapter) return adapter.handler();
        return new ChemicalWrapper(handler);
    }

    @Nullable
    private static IChemicalHandler adapt(@Nullable ResourceHandler<Resource> handler) {
        return handler == null ? null : new ChemicalAdapter(handler);
    }

    /** A chemical without an amount. */
    public static final class ChemicalResource implements Resource {
        static final ChemicalResource EMPTY = new ChemicalResource(ChemicalStack.EMPTY);

        private final ChemicalStack prototype;

        private ChemicalResource(ChemicalStack prototype) {
            this.prototype = prototype;
        }

        static ChemicalResource of(ChemicalStack stack) {
            return stack.isEmpty() ? EMPTY : new ChemicalResource(stack.copyWithAmount(1));
        }

        @Override
        public boolean isEmpty() {
            return prototype.isEmpty();
        }

        ChemicalStack toStack(long amount) {
            return prototype.copyWithAmount(amount);
        }

        boolean matches(ChemicalStack stack) {
            return ChemicalStack.isSameChemical(prototype, stack);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ChemicalResource other && ChemicalStack.isSameChemical(prototype, other.prototype);
        }

        @Override
        public int hashCode() {
            return prototype.isEmpty() ? 0 : prototype.getChemical().hashCode();
        }
    }

    private static int clamp(long value) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
    }

    private record ChemicalOp(int tank, ChemicalResource resource, int amount, boolean insert) {}

    /** Another mod's chemical handler: simulated inside transactions, moved for real on commit. */
    private static final class ChemicalWrapper extends Wrappers.Pending<ChemicalOp> implements ResourceHandler<Resource> {
        private final IChemicalHandler handler;

        ChemicalWrapper(IChemicalHandler handler) {
            this.handler = handler;
        }

        @Override public int size() { return handler.getChemicalTanks(); }

        @Override public Resource getResource(int index) { return ChemicalResource.of(handler.getChemicalInTank(index)); }

        @Override public long getAmountAsLong(int index) { return handler.getChemicalInTank(index).getAmount(); }

        @Override public long getCapacityAsLong(int index, Resource resource) { return handler.getChemicalTankCapacity(index); }

        @Override
        public boolean isValid(int index, Resource resource) {
            return resource instanceof ChemicalResource chemical && !chemical.isEmpty()
                    && handler.isValid(index, chemical.toStack(1));
        }

        private int waiting(int tank, ChemicalResource resource, boolean insert) {
            int total = 0;
            for (ChemicalOp op : ops) {
                if (op.tank() == tank && op.insert() == insert && op.resource().equals(resource)) total += op.amount();
            }
            return total;
        }

        @Override
        public int insert(int index, Resource resource, int amount, TransactionContext tx) {
            if (!(resource instanceof ChemicalResource chemical) || chemical.isEmpty() || amount <= 0) return 0;
            int waiting = waiting(index, chemical, true);
            ChemicalStack left = handler.insertChemical(index, chemical.toStack((long) waiting + amount), Action.SIMULATE);
            int accepted = Math.max(0, Math.min(amount, clamp((long) waiting + amount - left.getAmount()) - waiting));
            if (accepted > 0) add(new ChemicalOp(index, chemical, accepted, true), tx);
            return accepted;
        }

        @Override
        public int extract(int index, Resource resource, int amount, TransactionContext tx) {
            if (!(resource instanceof ChemicalResource chemical) || chemical.isEmpty() || amount <= 0
                    || !chemical.matches(handler.getChemicalInTank(index))) return 0;
            int waiting = waiting(index, chemical, false);
            ChemicalStack got = handler.extractChemical(index, (long) waiting + amount, Action.SIMULATE);
            if (!got.isEmpty() && !chemical.matches(got)) return 0;
            int taken = Math.max(0, Math.min(amount, clamp(got.getAmount()) - waiting));
            if (taken > 0) add(new ChemicalOp(index, chemical, taken, false), tx);
            return taken;
        }

        @Override
        protected void run(ChemicalOp op) {
            if (op.insert()) {
                ChemicalStack left = handler.insertChemical(op.tank(), op.resource().toStack(op.amount()), Action.EXECUTE);
                if (!left.isEmpty()) LOGGER.warn("{} took less than it said it would: {} lost", handler, left);
            } else {
                ChemicalStack got = handler.extractChemical(op.tank(), op.amount(), Action.EXECUTE);
                if (got.getAmount() != op.amount()) LOGGER.warn("{} gave {} instead of {}", handler, got, op.amount());
            }
        }
    }

    /** Our chemical handler as a Mekanism one, each call in a transaction of its own. */
    private record ChemicalAdapter(ResourceHandler<Resource> handler) implements IChemicalHandler {
        @Override public int getChemicalTanks() { return handler.size(); }

        @Override
        public ChemicalStack getChemicalInTank(int tank) {
            return handler.getResource(tank) instanceof ChemicalResource chemical && !chemical.isEmpty()
                    ? chemical.toStack(handler.getAmountAsLong(tank)) : ChemicalStack.EMPTY;
        }

        @Override
        public void setChemicalInTank(int tank, ChemicalStack stack) {
            // Not supported: our handlers only move chemicals through insert and extract.
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return handler.getCapacityAsLong(tank, handler.getResource(tank));
        }

        @Override
        public boolean isValid(int tank, ChemicalStack stack) {
            return handler.isValid(tank, ChemicalResource.of(stack));
        }

        @Override
        public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            if (stack.isEmpty()) return stack;
            int offered = clamp(stack.getAmount());
            int inserted;
            try (Transaction tx = Transaction.openAny()) {
                inserted = handler.insert(tank, ChemicalResource.of(stack), offered, tx);
                if (action.execute()) tx.commit();
            }
            return inserted >= stack.getAmount() ? ChemicalStack.EMPTY : stack.copyWithAmount(stack.getAmount() - inserted);
        }

        @Override
        public ChemicalStack extractChemical(int tank, long amount, Action action) {
            if (!(handler.getResource(tank) instanceof ChemicalResource chemical) || chemical.isEmpty() || amount <= 0) {
                return ChemicalStack.EMPTY;
            }
            int extracted;
            try (Transaction tx = Transaction.openAny()) {
                extracted = handler.extract(tank, chemical, clamp(amount), tx);
                if (action.execute()) tx.commit();
            }
            return extracted <= 0 ? ChemicalStack.EMPTY : chemical.toStack(extracted);
        }
    }
}
