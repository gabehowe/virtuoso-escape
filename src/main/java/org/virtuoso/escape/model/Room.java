package org.virtuoso.escape.model;

import java.util.ArrayList;
import java.util.List;

public record Room(ArrayList<Entity> entities, String id, String introMessage){
    public String name() {
        return GameInfo.instance().string(this.id, "name");
    }
}