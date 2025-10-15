package org.virtuoso.escape.model;

import java.util.List;

/**
 * @author gabri
 * NOTE record automatically generates getters.
 */
public record Floor(String id, List<Room> rooms) {
}