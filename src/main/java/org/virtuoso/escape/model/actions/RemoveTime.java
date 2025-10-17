package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameState;

import java.time.Duration;

public record RemoveTime(Severity sevarity) implements Action{
    @Override
    public void execute() {
		final int BASE_TIME = 5;
		Duration penalty = Duration.ofSeconds(BASE_TIME);
		switch (sevarity) {case HIGH: penalty.multipliedBy(3); case MEDIUM: penalty.multipliedBy(2); case LOW:;}
		switch (GameState.instance().difficulty()) {case VIRTUOSIC: penalty.multipliedBy(3); case SUBSTANTIAL: penalty.multipliedBy(2); case TRIVIAL:;}
        GameState.instance().addTime(penalty.negated());
    }
}

