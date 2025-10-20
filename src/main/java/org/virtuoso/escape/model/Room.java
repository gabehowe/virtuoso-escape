package org.virtuoso.escape.model;

import java.util.List;

/**
 * A collection of entities and an introductory message.
 * @param entities The list of entities in the room.
 * @param id The id of the room.
 * @param introMessage The message to introduce with.
 */
public record Room(List<Entity> entities, String id, String introMessage){
    /**
     * The string resource name of the room.
     * @return The string resource name of the room.
     */
    public String name() {
        return GameInfo.instance().string(this.id, "name");
    }
}