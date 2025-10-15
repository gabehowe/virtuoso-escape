package org.virtuoso.escape.model;

import java.util.List;

public record Room(List<Entity> entities, String id, String introMessage){
}