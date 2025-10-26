package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameState;

import java.time.Duration;

/**
 * Penalize the player by removing time.
 *
 * @param severity The degree of penalty to award
 * @author Andrew
 */
public record AddPenalty(Severity severity) implements Action {
    /**
     * Apply the penalty.
     */
    @Override
    public void execute() {
        int points = 5;
        points =
                switch (severity) {
                    case HIGH -> points * 3;
                    case MEDIUM -> points * 2;
                    case LOW -> points;
                };
        points =
                switch (GameState.instance().difficulty()) {
                    case VIRTUOSIC -> points * 3;
                    case SUBSTANTIAL -> points * 2;
                    case TRIVIAL -> points;
                };
        GameState.instance().addPenalty(points);
    }
}

