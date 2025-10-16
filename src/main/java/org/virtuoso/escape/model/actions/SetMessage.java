package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;

/**
 * @author gabri
 * @param message
 */
public record SetMessage(String message) implements Action {

    // Allows usage in other places
    public SetMessage(GameInfo instance, String id, String stringId) {
        this(instance.string(id, stringId));
    }
    @Override
    public void execute() {
        GameState.instance().setCurrentMessage(message);
    }
}
