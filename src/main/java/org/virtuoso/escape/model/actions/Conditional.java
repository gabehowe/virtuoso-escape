package org.virtuoso.escape.model.actions;

import java.util.function.BooleanSupplier;

/**
 * Control flow action.
 * Runs {@code if_} if {@code condition}, else runs {@code else_}.
 * @param condition A boolean-returning function to check.
 * @param if_ The action to run if the condition returns true.
 * @param else_ The action to run if the condition returns false.
 * @author gabri
 */
public record Conditional(BooleanSupplier condition, Action if_, Action else_) implements Action {
    // Overloaded second operator for
    public Conditional(BooleanSupplier condition, Action if_) {
        this(condition, if_, null);
    }

    @Override
    public void execute() {
        if (condition.getAsBoolean()) if_.execute();
        else if (else_ != null) else_.execute();

    }
}
