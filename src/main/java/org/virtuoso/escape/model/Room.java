package org.virtuoso.escape.model;

import java.util.ArrayList;

public record Room(ArrayList<Entity> entities, String id, String introMessage){
}