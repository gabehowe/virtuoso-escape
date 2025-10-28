package org.virtuoso.escape.model.action;

import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;

/**
 * Move to a floor and clear items.
 *
 * @param floor The number of the floor to move to.
 * @author Andrew
 */
public record SetFloor(int floor) implements Action {
    /**
     * Perform the floor change.
     */
    @Override
    public void execute() {
        GameState.instance().setCurrentFloor(GameInfo.instance().building().get(floor));
        GameState.instance().clearItems();
    }
}
