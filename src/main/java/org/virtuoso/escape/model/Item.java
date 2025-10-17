package org.virtuoso.escape.model;

public enum Item {

    // Floor 1 items
    left_bread("left bread");

    private final String name;

    Item(String name) {
        this.name = name;
    }

    public String itemName() {
        return this.name;
    }

    public String id() {
        return this.toString();
    }
}