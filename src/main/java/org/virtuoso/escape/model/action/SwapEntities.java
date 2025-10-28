package org.virtuoso.escape.model.action;

import org.virtuoso.escape.model.Entity;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Room;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Changes the state of an entity
 *
 * @param entity The entity to change the state of
 * @param state  The state to set the entity to
 * @author gabri
 * @author Andrew
 */
public record SwapEntities(String entity, String state) implements Action {
    @Override
    public void execute() {
        List<Room> rooms = GameState.instance().currentFloor().rooms();
        for (var room : rooms) {
            List<Entity> ents = room.entities();
            Optional<Entity> entityFSM = ents.stream().filter(entity -> Objects.equals(entity.id(), entity())).findFirst();
            entityFSM.ifPresent(e -> e.swapState(state));
        }
    }
}
