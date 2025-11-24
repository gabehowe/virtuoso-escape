package org.virtuoso.escape.model.action;

import java.util.Objects;
import org.virtuoso.escape.model.Entity;
import org.virtuoso.escape.model.GameState;

/**
 * Changes the state of an entity
 *
 * @param entity The entity to change the state of
 * @param state The state to set the entity to
 * @author gabri
 * @author Andrew
 */
public record SwapEntities(String entity, String state) implements Action {
    @Override
    public void execute() {
        if (entity == null) {
            throw new IllegalArgumentException("Entity ID to swap cannot be null");
        }
        Entity entityToSwap = GameState.instance().currentFloor().rooms().stream()
                .flatMap(room -> room.entities().stream())
                .filter(e -> Objects.equals(e.id(), entity()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Entity with id '" + entity + "' not found in current floor."));

        entityToSwap.swapState(state);
    }
}
