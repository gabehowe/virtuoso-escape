package org.virtuoso.escape.model;

import java.util.ArrayList;

/**
 * @author gabri
 * NOTE record automatically generates getters.
 */
public class Floor {
    private String id;
    private ArrayList<Room> rooms;
    public Floor(String id, ArrayList<Room> rooms){
        this.id = id;
        this.rooms = rooms;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }
}