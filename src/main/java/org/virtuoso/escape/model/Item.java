package org.virtuoso.escape.model;

/**
 * An enumeration of items to be held in an inventory list.
 * @author Bose
 */
public enum Item {
    //Floor 1 items//
    left_bread("left bread"),
    sunflower_seed_butter("sunflower seed butter"),
    right_bread("right bread"),
	sealed_clean_food_safe_hummus("sealed clean food-safe hummus");

    private final String name;

    /**
     * Construct an item with a name.
     * @param name The name to construct with.
     */
    Item(String name) {
        this.name = name;
    }

    /**
     * The name of the item.
     * @return The name of the item.
     */
    public String itemName() {
        return this.name;
    }

    /**
     * The id of the item.
     * @return The id of the item.
     */
    public String id() {
        return this.toString();
    }
}