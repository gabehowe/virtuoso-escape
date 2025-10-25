package org.virtuoso.escape.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of entities and an introductory message.
 *
 * @param id           The id of the room.
 * @param entities     The list of entities in the room.
 * @param introMessage The message to introduce with.
 */
public record Room(String id, List<Entity> entities, String introMessage) {
    /**
     * Initialize with default introduce.
     * @param info     The GameInfo instance to use.
     * @param id       The id of the room.
     * @param entities The entities in the room.
     */
    public Room(GameInfo info, String id, Entity... entities) {
        this(id, new ArrayList<>(Arrays.stream(entities).toList()), info.string(id, "introduce"));
    }
    /**
     * The string resource name of the room.
     *
     * @return The string resource name of the room.
     */
    public String name() {
        return GameInfo.instance().string(this.id, "name");
    }
}
