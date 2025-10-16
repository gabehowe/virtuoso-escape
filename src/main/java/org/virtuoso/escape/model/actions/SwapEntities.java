package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SwapEntities(Entity toPlace, String toReplace) implements Action {
	@Override
	public void execute() {
		List<Room> rooms = GameState.instance().currentFloor().rooms();
		for (var room : rooms) {
			List<Entity> ents = room.entities();
			if (ents.stream().noneMatch((ent) -> Objects.equals(ent.id(), toPlace.id()))) continue;
			ents.set(ents.indexOf(toReplace), toPlace); // TODO(gabri) test this and see if toReplace has somehow been copied.
		}
	}
}
