package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameState;

import java.time.Duration;

/**
 * Penalize the player by removing time.
 *
 * @param severity The degree of penalty to award
 * @author Andrew
 */
public record RemoveTime(Severity severity) implements Action {
    /**
     * Apply the penalty.
     */
    @Override
    public void execute() {
        final int BASE_TIME = 5;
        Duration penalty = Duration.ofSeconds(BASE_TIME);
        penalty =
                switch (severity) {
                    case HIGH -> penalty.multipliedBy(3);
                    case MEDIUM -> penalty.multipliedBy(2);
                    case LOW -> penalty;
                };
        penalty =
                switch (GameState.instance().difficulty()) {
                    case VIRTUOSIC -> penalty.multipliedBy(3);
                    case SUBSTANTIAL -> penalty.multipliedBy(2);
                    case TRIVIAL -> penalty;
                };
        GameState.instance().addTime(penalty.negated());
    }
}

