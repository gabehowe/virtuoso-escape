package org.virtuoso.escape.model;

import java.util.List;

/**
 * A grouping of rooms with an id.
 * @author gabri
 * NOTE record automatically generates getters.
 */
public record Floor(String id, List<Room> rooms) {
}