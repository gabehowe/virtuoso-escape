package org.virtuoso.escape.model.action;

import java.util.function.BooleanSupplier;

/**
 * Control flow action.
 * Runs {@code if_} if {@code condition}, else runs {@code else_}.
 *
 * @param condition A boolean-returning function to check.
 * @param if_       The action to run if the {@code condition } returns true.
 * @param else_     The action to run if the {@code condition } returns false.
 * @author gabri
 */
public record Conditional(BooleanSupplier condition, Action if_, Action else_) implements Action {
    /**
     * A conditional with no default action.
     *
     * @param condition A boolean-returning function to check.
     * @param if_       The action to run if the {@code condition} returns true.
     */
    public Conditional(BooleanSupplier condition, Action if_) {
        this(condition, if_, null);
    }

    /**
     * Run {@code if_} if {@code condition} is {@code true}, else run {@code else_}
     */
    @Override
    public void execute() {
        if (condition.getAsBoolean()) if_.execute();
        else if (else_ != null) else_.execute();
    }
}
