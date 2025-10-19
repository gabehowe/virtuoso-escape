package org.virtuoso.escape.model.actions;

import java.util.Arrays;

/**
 * Represents chained action functions.
 * @param actions The actions to run in the order they are input.
 * @author gabri
 */
public record Chain(Action... actions) implements Action{
    /**
     * Execute the actions in the order they were declared.
     */
    @Override
    public void execute() {
        Arrays.stream(actions).forEach(Action::execute);
    }
}
