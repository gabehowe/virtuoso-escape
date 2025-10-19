package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Swap two entities
 * @param toPlace The entity to replace the entity to replace with.
 * @param toReplace The entity to replace.
 * @author gabri
 */
public record SwapEntities(Entity toPlace, String toReplace) implements Action {
	@Override
	public void execute() {
		List<Room> rooms = GameState.instance().currentFloor().rooms();
		for (var room : rooms) {
			List<Entity> ents = room.entities();
			Optional<Entity> replace = ents.stream().filter(entity -> Objects.equals(entity.id(), toReplace)).findFirst();
			replace.ifPresent(e->e.absorb(toPlace));
		}
	}
}
