package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SwapEntities(Entity toPlace, Entity toReplace) implements Action {
	@Override
	public void execute() {
		// TODO(gabri) figure out how do this
		List<Room> rooms = GameState.instance().currentFloor().rooms();
		List<String> j;
		// TODO(gabri) don't throw if the room doesn't exist.
		for (var room : rooms) {
			List<Entity> ents = room.entities();
			if (ents.stream().noneMatch((ent) -> Objects.equals(ent.id(), toPlace.id()))) continue;
			ents.set(ents.indexOf(toReplace), toPlace); // TODO(gabri) test this and see if toReplace has somehow been copied.
		}
	}
}
