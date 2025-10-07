package org.virtuoso.escape.model.actions;

import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;

public record GiveItem(Item item) implements Action {
	@Override
	public void execute() {
		GameState.instance().addItem(item);
	}
}
