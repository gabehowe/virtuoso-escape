package org.virtuoso.escape.model;

import org.virtuoso.escape.model.actions.Action;
import org.virtuoso.escape.model.actions.GiveItem;
import org.virtuoso.escape.model.actions.SwapEntities;
import org.virtuoso.escape.model.actions.TakeInput;
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
    private Map<String, Map<String, String>> language = Map.of();
    public List<Floor> building = new ArrayList<Floor>();

    public static GameInfo instance() {
        if (instance == null)
            instance = new GameInfo();
        return instance;
    }

    private void gameEnding_moral() {
        throw new RuntimeException("unimplemented!");
    }

    private void gameEnding_immoral() {
        throw new RuntimeException("unimplemented!");
    }

    private Entity makeComputtyChain() {
        // Whoops! JEP 126!
        Entity microwaveUnblocked = new Entity("microwave_unblocked", this::gameEnding_moral, null, this::gameEnding_immoral, null);
        var computtyTarLogic = new TakeInput("", Map.of(
                "rotx 16 code", new SwapEntities(microwaveUnblocked, "microwave_blocked")));
        var computtyTar = new Entity("computty_tar", null, null, null, computtyTarLogic);
        var computtyCdLogic = new TakeInput("", Map.of(
                "tar -xvf code.tar", new SwapEntities(computtyTar, "computty_cd")
        ));
        Entity computtyCd = new Entity("computty_cd", null, null, null, computtyCdLogic);
        var computtyDefault = new TakeInput("", Map.of(
                "cd", new SwapEntities(computtyCd, "computty")
        ));
        return new Entity("computty", null, null, null, computtyDefault);
    }

    private Floor floor3() {
        // Basic info entity -- provide logic by adding dialogue
        Entity man = new Entity("man", null, null, null, null);
        Entity sock_squirrel = new Entity("sock_squirrel", new GiveItem(Item.TTY_PASSWORD), null, null, null);
        Entity computty = makeComputtyChain();
        Entity microwave = new Entity("microwave_blocked", null, null, null, null);
        Room floor3_0 = new Room(List.of(man, sock_squirrel, computty, microwave), "floor3_0", this.string("floor3_0", "intro"));
        return new Floor("floor3", List.of(floor3_0));
    }


    private Floor acornGrove() {
        Entity intro_squirrel = new Entity("intro_squirrel", null, null, null, null);
        Entity portal_squirrel = new Entity("portal_squirrel",
                null,
                null,
                () -> GameState.instance().setCurrentFloor(this.building.get(1)
                ), null);
        Room acornGrove_0 = new Room(List.of(intro_squirrel, portal_squirrel), "acorn_grove_0", this.string("acorn_grove_0", "intro"));
        return new Floor("acorn_grove", List.of(acornGrove_0));
    }

    private GameInfo() {
        // todo: add other floors
        this.language = DataLoader.loadGameLanguage();

        this.building.add(acornGrove());
        // TODO: floor 1
        // TODO: floor 2
        this.building.add(floor3());
    }

    ;

    public String string(String id, String stringId) {
        if (!language.containsKey(id) || !language.get(id).containsKey(stringId)) return "[" + id + "/" + stringId + "]"; // Default behavior for string
        return language.get(id).get(stringId);
    }

    public Map<String, Map<String, String>> language() {
        return language;
    }

}