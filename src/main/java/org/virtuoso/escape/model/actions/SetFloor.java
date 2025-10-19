package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;

public record SetFloor(int floor) implements Action{
    @Override
    public void execute() {
        GameState.instance().setCurrentFloor(GameInfo.instance().building().get(floor));
    }
}
