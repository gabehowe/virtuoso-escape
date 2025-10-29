package org.virtuoso.escape.model.action;

import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;

/**
 * Gives an item to the user.
 *
 * @param item The item to give to the user.
 * @author gabri
 */
public record GiveItem(Item item) implements Action {
    /** Give the item. */
    @Override
    public void execute() {
        GameState.instance().addItem(item);
    }
}
