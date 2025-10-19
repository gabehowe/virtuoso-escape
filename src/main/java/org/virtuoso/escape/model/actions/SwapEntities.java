package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Changes the state of an entity
 * @author gabri
 * @author Andrew
 * @param entity The entity to change the state of
 * @param state The state to set the entity to
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
