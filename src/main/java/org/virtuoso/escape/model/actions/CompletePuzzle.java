package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameState;

/**
 * Add a puzzle to the completed list.
 *.
 * @author Andrew
 */
public record CompletePuzzle(String puzzle) implements Action {
    /**
     * Execute the actions in the order they were declared.
     */
    @Override
    public void execute() {
        GameState.instance().addCompletedPuzzle(puzzle);
    }
}
