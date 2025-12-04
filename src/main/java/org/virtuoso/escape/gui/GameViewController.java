package org.virtuoso.escape.gui;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import org.json.simple.JSONArray;
import org.virtuoso.escape.model.*;
import org.w3c.dom.events.EventTarget;

/**
 * Controller for the game screen. Interfaces with WebView javascript engine.
 *
 * @author gabri
 */
public class GameViewController implements Initializable {
    public GameProjection projection;
    private Floor lastFloor;
    private Entity lastEntity;
    // grandfathered in, so no longer temporary
    // we also can't remove this temporary value because industry depends on it, and we don't want to ruin backwards
    // compatibility
    private EventHandler<KeyEvent> ourFunTemporaryEventHandlerReference;
    private String dialogueSuffix = "\nPress any button to continue...";

    @FXML
    public WebView webView;

    /**
     * Initialize the view.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("game-view.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> {
            webView.getEngine().executeScript("init()");
            updateAll();
        });
        projection = App.projection;
    }

    /** Change the background image and display all entities. */
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

    /** Update the dialogue box and entity title. */
    public void updateDialogue() {
        var switchedFloor = lastFloor != null && lastFloor != projection.currentFloor() || projection.isEnded();
        setDialogue(projection
                        .currentMessage()
                        .orElse(projection
                                .currentEntity()
                                .map(s -> s.string("introduce"))
                                .orElse(projection.currentRoom().introMessage()))
                + (switchedFloor ? dialogueSuffix : ""));
        AtomicReference<String> name = new AtomicReference<>(projection
                .currentEntity()
                .map(s -> s.string("name"))
                .orElse(projection.currentRoom().name()));
        if (switchedFloor) Optional.ofNullable(lastEntity).ifPresent(jim -> name.set(jim.string("name")));
        App.setText(webView.getEngine(), "entity-title", name.get());
    }

    /**
     * Update a left side box.
     *
     * @param id The id of the box to update.
     * @param current The currently selected element name (or "undefined").
     * @param names All elements names including current.
     * @param button Whether the eleemnts should be buttons.
     */
    public void updateBox(String id, String current, List<List<String>> names, boolean button) {
        var mapped = new JSONArray();
        mapped.addAll(names);
        App.callJSFunction(webView.getEngine(), "updateBox", id, current, mapped, button);
    }

    /** Update all left bar boxes. */
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
            theRoom.ifPresent(rm -> ((EventTarget) it)
                    .addEventListener(
                            "click",
                            e -> {
                                projection.pickRoom(rm);
                                updateAll();
                            },
                            false));
        });

        var entityNames = projection.currentRoom().entities().stream()
                .map(it -> List.of(it.string("name"), it.state().id()))
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
                                projection.pickEntity(ent);
                                updateAll();
                            },
                            false));
        });

        var itemNames = projection.currentItems().stream()
                .map(it -> List.of(it.itemName(), it.id()))
                .toList();
        updateBox("item-box", "undefined", itemNames, false);
    }

    /** Update the action box. */
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

    /** Update all portions of the game screen. */
    public void updateAll() {
        updateDialogue();
        // Keep the dialogue and wait for a key.
        if (lastFloor != null && lastFloor != projection.currentFloor()) {

            ourFunTemporaryEventHandlerReference = event -> {
                lastFloor = projection.currentFloor();
                lastEntity = null;
                updateAll();
                event.consume();
                webView.removeEventFilter(KeyEvent.KEY_PRESSED, ourFunTemporaryEventHandlerReference);
                ourFunTemporaryEventHandlerReference = null;
            };
            webView.addEventFilter(KeyEvent.KEY_PRESSED, ourFunTemporaryEventHandlerReference);
            return;
        }
        updateLeftBar();
        updateCapabilities();
        updateImage();
        App.callJSFunction(webView.getEngine(), "createKeys");
        if (projection.isEnded()) {
            ourFunTemporaryEventHandlerReference = event -> {
                try {
                    updateDialogue();
                    App.setRoot("credits");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            webView.addEventFilter(KeyEvent.KEY_PRESSED, ourFunTemporaryEventHandlerReference);
            return;
        }

        lastFloor = projection.currentFloor();
        lastEntity = projection.currentEntity().orElse(null);
    }

    /** Inspect and update all elements. */
    public void inspect() {
        projection.inspect();
        updateAll();
    }

    /** Interact and update all elements. */
    public void interact() {
        projection.interact();
        updateAll();
    }

    /** Attack and update all elements. */
    public void attack() {
        projection.attack();
        updateAll();
    }

    /**
     * Attempt to speak to the selected entity.
     *
     * @param input The message to send.
     */
    public void input(Object input) {
        projection.input(input.toString());
        updateAll();
    }

    /** Close the application. */
    public void exit() {
        App.exit();
    }

    /**
     * Set a message in the dialogue window.
     *
     * @param text The message to set.
     */
    private void setDialogue(String text) {
        App.callJSFunction(webView.getEngine(), "setDialogue", App.sanitizeForJS(text));
    }

    /**
     * Return the current remaining time.
     *
     * @return The current remaining time.
     */
    public String getTime() {
        return String.format(
                "{%02d:%02d}", projection.time().toMinutes(), projection.time().toSecondsPart());
    }

    /**
     * Change the difficulty to the string ID
     *
     * @param difficultyID The difficulty change to.
     * @throws IllegalArgumentException if the difficulty id is not a valid difficulty.
     */
    public void pickDifficulty(String difficultyID) throws IllegalArgumentException {
        projection.setDifficulty(Difficulty.valueOf(difficultyID));
    }

    /// DEBUG
    /**
     * Returns all floors in an array.
     *
     * @return An array of floors.
     */
    public String[] debugGetFloors() {
        return GameInfo.instance().building().stream().map(Floor::id).toArray(String[]::new);
    }

    /**
     * Change to a specific floor.
     *
     * @param floor The floor to change to.
     */
    public void debugSetFloor(String floor) {
        GameState.instance()
                .setCurrentFloor(GameInfo.instance().building().stream()
                        .filter(it -> Objects.equals(it.id(), floor))
                        .findFirst()
                        .get());
        updateAll();
    }

    /** End the game. */
    public void debugEndGame() {
        GameState.instance().end();
    }

    /**
     * Returns the current difficulty.
     *
     * @return The current difficulty.
     */
    public String debugCheckDifficulty() {
        return GameState.instance().difficulty().toString();
    }
}
