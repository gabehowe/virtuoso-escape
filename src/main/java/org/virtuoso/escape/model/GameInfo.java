package org.virtuoso.escape.model;

import org.virtuoso.escape.model.data.DataLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew
 */
public class GameInfo {
    private static GameInfo instance;
    private Map<String, Map<String,String>> language = Map.of();
    public List<Floor> building = new ArrayList<Floor>();

    public static GameInfo instance() {
        if (instance == null) 
            instance = new GameInfo();
        return instance;
    }

    private Floor acornGrove(){
        Room acornGrove_0 = new Room(List.of(), "acorn_grove_0", this.string("acorn_grove_0", "intro"));
        Floor acornGrove = new Floor("acorn_grove", List.of(acornGrove_0));
        return acornGrove;
    }

    private GameInfo() {
        // todo: add other floors
        this.language = DataLoader.loadGameLanguage();

        this.building.add(acornGrove());
    };

    public String string(String id, String stringId) {
        // Will throw exception if string not found
        return language.get(id).get(stringId);
    }

    public Map<String,Map<String,String>> language(){
        return language;
    }

}