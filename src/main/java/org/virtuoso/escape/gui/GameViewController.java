package org.virtuoso.escape.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.json.simple.JSONArray;
import org.virtuoso.escape.model.*;
import org.w3c.dom.events.EventTarget;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class GameViewController implements Initializable {
    public GameProjection projection;

    @FXML
    public WebView webView;

    public void updateImage() {
        var entities =
                projection.currentRoom().entities().stream().map(Entity::id).toList();
        var arr = new JSONArray();
        arr.addAll(entities);
        App.callJSFunction(
                webView.getEngine(),
                "populateBackground",
                projection.currentEntity().map(Entity::id).orElse("undefined"),
                arr);
        App.callJSFunction(
                webView.getEngine(), "setRoomImage", projection.currentRoom().id());
    }

    public void updateDialogue() {
        setDialogue(projection
                .currentMessage()
                .orElse(projection
                        .currentEntity()
                        .map(s -> s.string("introduce"))
                        .orElse(projection.currentRoom().introMessage())));
        App.setText(
                webView.getEngine(),
                "entity-title",
                projection
                        .currentEntity()
                        .map(s -> s.string("name"))
                        .orElse(projection.currentRoom().name()));
    }

    public void updateBox(String name, String current, List<List<String>> names, boolean button) {
        var mapped = new JSONArray();
        mapped.addAll(names);
        App.callJSFunction(webView.getEngine(), "updateBox", name, current, mapped, button);
    }

    public void updateLeftBar() {
        var roomNames = new ArrayList<>(projection.currentFloor().rooms().stream()
                                                  .map(rm -> List.of(rm.name(), rm.id()))
                                                  .toList());
        updateBox("map-box", projection.currentRoom().id(), roomNames, true);
        var mapElements = App.querySelectorAll(webView.getEngine(), "#map-box > .box-element");
        mapElements.forEach(it -> {
            var theRoom = projection.currentFloor().rooms().stream()
                                    .filter(rm -> Objects.equals(rm.id(), it.getAttribute("id")))
                                    .findFirst();
            theRoom.ifPresent(rm -> ((EventTarget) it).addEventListener("click", e -> this.pickRoom(rm), false));
        });

        var entityNames = projection.currentRoom().entities().stream()
                                    .map(it -> List.of(
                                            it.string("name"),
                                            it.state().id()))
                                    .toList();
        var currentEntity = projection.currentEntity().map(e -> e.state().id()).orElse("undefined");
        updateBox("entity-box", currentEntity, entityNames, true);

        var entities = App.querySelectorAll(webView.getEngine(), "#entity-box > .box-element");
        entities.forEach(it -> {
            var theEntity = projection.currentRoom().entities().stream()
                                      .filter(ent -> Objects.equals(ent.state().id(), it.getAttribute("id")))
                                      .findFirst();
            theEntity.ifPresent(ent -> ((EventTarget) it)
                    .addEventListener(
                            "click",
                            e -> {
                                this.pickEntity(ent);
                            },
                            false));
        });

        var itemNames = projection.currentItems().stream()
                                  .map(it -> List.of(it.itemName(), it.id()))
                                  .toList();
        updateBox("item-box", "undefined", itemNames, false);
    }

    public void updateCapabilities() {
        var cap = projection
                .currentEntity()
                .map(it -> it.state().capabilities())
                .orElse(new EntityState.Capabilities(false, false, false, false));
        var interact = webView.getEngine().getDocument().getElementById("interact");
        interact.setAttribute("style", cap.interact() ? "" : "display:none;");
        webView.getEngine()
               .getDocument()
               .getElementById("inspect")
               .setAttribute("style", (cap.inspect() ? "" : "display:none;"));
        webView.getEngine()
               .getDocument()
               .getElementById("attack")
               .setAttribute("style", (cap.attack() ? "" : "display:none;"));
        webView.getEngine()
               .getDocument()
               .getElementById("speak")
               .setAttribute("style", (cap.input() ? "" : "display:none;"));
        webView.getEngine().executeScript("");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projection = App.projection;
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("game-view.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> {
            var doc = webView.getEngine().getDocument();

            //            var inspect = doc.getElementById("inspect");
            webView.getEngine().executeScript("init()");
            //            ((EventTarget) doc).addEventListener("load", e -> webView.getEngine().executeScript("init()"),
            // false);
            //            ((EventTarget) doc).addEventListener("keydown", e -> webView.getEngine().executeScript(""),
            // false);
            updateAll();
        });
    }

    public void updateAll() {
        updateLeftBar();
        updateCapabilities();
        updateDialogue();
        updateButtons();
        updateImage();
    }

    public void updateButtons() {
        webView.getEngine().executeScript("createKeys()");
    }

    public void pickEntity(Entity ent) {
        projection.pickEntity(ent);
        updateAll();
    }

    public void pickRoom(Room room) {
        projection.pickRoom(room);
        updateAll();
    }

    public void inspect() {
        System.out.println("Inspect!");
        projection.inspect();
        updateAll();
    }

    public void interact() {
        System.out.println("Interact!");
        projection.interact();
        updateAll();
    }

    public void attack() {
        System.out.println("Attack!");
        projection.attack();
        updateAll();
    }

    public void input(Object input) {
        System.out.println("Create input!");
        projection.input(input.toString());
        updateAll();
    }

    public void toggleTTS() {
        // TODO: make
    }

    public void exit() {
        App.exit();
    }

    private void setDialogue(String text) {
        App.callJSFunction(webView.getEngine(), "setDialogue", App.sanitizeForJS(text));
    }

    /// DEBUG
    public String getFloors() {
        var array = new JSONArray();
        array.addAll(GameInfo.instance().building().stream().map(Floor::id).toList());
        return array.toJSONString();
    }

    public void setFloor(Object floor) {
        GameState.instance()
                 .setCurrentFloor(GameInfo.instance().building().stream()
                                          .filter(it -> Objects.equals(it.id(), floor.toString()))
                                          .findFirst()
                                          .get());
        updateAll();
    }

    public void endGame() {
        GameState.instance().end();
    }

    public String getTime() {
        return "{" + projection.time().toMinutes() + ":" + projection.time().toSecondsPart() + "}";
    }
}
