package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;

/**
 * @param message The message to give
 * @author gabri
 */
public record SetMessage(String message) implements Action {

    /**
     * Set the message from a resource.
     *
     * @param instance Usually the global GameInfo instance.
     * @param id       The name of the object which stores the id.
     * @param stringId the specific string to get from the object.
     */
    public SetMessage(GameInfo instance, String id, String stringId) {
        this(instance.string(id, stringId));
    }

    /**
     * Set the message
     */
    @Override
    public void execute() {
        GameState.instance().setCurrentMessage(message);
    }
}
