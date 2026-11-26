package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.InteractionResult;

/** Stand-in for 1.20.5+'s result of using an item on a block. See {@link BlockInteractions}. */
public enum ItemInteractionResult {
    SUCCESS(InteractionResult.SUCCESS),
    CONSUME(InteractionResult.CONSUME),
    CONSUME_PARTIAL(InteractionResult.CONSUME_PARTIAL),
    PASS_TO_DEFAULT_BLOCK_INTERACTION(InteractionResult.PASS),
    SKIP_DEFAULT_BLOCK_INTERACTION(InteractionResult.PASS),
    FAIL(InteractionResult.FAIL);

    private final InteractionResult result;

    ItemInteractionResult(InteractionResult result) {
        this.result = result;
    }

    public InteractionResult result() {
        return result;
    }

    public boolean consumesAction() {
        return result.consumesAction();
    }

    public static ItemInteractionResult sidedSuccess(boolean clientSide) {
        return clientSide ? SUCCESS : CONSUME;
    }
}
