package org.virtuoso.escape.gui;

// JEP 511

import module javafx.fxml;
import module javafx.graphics;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.virtuoso.escape.model.GameProjection;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class App extends Application {

    private static Scene scene;
    public static GameProjection projection;

    @Override
    public void start(Stage stage) throws IOException {
        projection = new GameProjection();
        FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
        loginLoader.setController(new LoginController(projection));

        scene = new Scene(loginLoader.load());
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void setApp(WebEngine engine, Object app, Runnable callback) {
        engine.documentProperty().addListener((obs, old, newDoc) -> {
            System.out.println("Succeded!");
            var window = (JSObject) engine.executeScript("window");
            window.setMember("app", app);
            window.setMember("logger", new Logger());
            engine.executeScript("window.onerror = logger.log;" + "console.error = function(message) { app.error(message); };");
            callback.run();
        });
    }

    public static void setText(WebEngine engine, String elementId, String text) {
        var cmd = String.format("document.getElementById('%s').textContent=\"%s\";", elementId,
                text.replace("\"", "\\\"")
                    .replace("\n", "<br>"))
                .replace("\t", "\\t");
        Logger.log(cmd);
        engine.executeScript(cmd);
    }

    public static class Logger {
        public static void log(Object msg) {
            System.out.println(msg);
        }
    }

    public static ArrayList<Element> querySelectorAll(WebEngine engine, String selector) {
        var elements = (Integer) engine.executeScript("j = document.querySelectorAll('" + selector + "'); j.length");
        var list = new ArrayList<Element>();
        for (int i = 0; i < elements; i++) {
            list.add((Element) engine.executeScript("j[" + i + "]"));
        }
        return list;
    }

    public static Object callJSFunction(WebEngine engine, String function, Object... args) {
        var jsArgs = Arrays.stream(args).map(it -> {
            if (it instanceof String j) {
                return "\"" + j + "\"";
            }
            return it;
        }).map(Object::toString).collect(Collectors.joining(","));
        var cmd = String.format("%s(%s);", function, jsArgs);
        Logger.log(cmd);
        return engine.executeScript(cmd);
    }

    public static EventHandler<KeyEvent> keyboardHandler;
    public static EventHandler<KeyEvent> keyboardReleaseHandler;

    public static void addKeyboardBindings(WebEngine engine, WebView view) {
        var actions = querySelectorAll(engine, "#action-box > .logical-button");
        var buttons = querySelectorAll(engine, ".logical-button");
        var selected = querySelectorAll(engine, ".selected");
        Map<String, Element> keyMap = new LinkedHashMap<>();
        var fixed = buttons.stream().filter(it -> it.hasAttribute("keyboard")).toList();

        BiFunction<String, Element, String> findValidKey = (sourceKey, elem) -> {
            if (keyMap.containsValue(elem)) {
                var key = keyMap.keySet().stream().filter(it -> keyMap.get(it) == elem).findFirst().get();
                // Keep correct case
                return String.valueOf(elem.getTextContent().charAt(elem.getTextContent().toLowerCase().indexOf(key)));
            }
            String key;
            int index = 0;
            for (var c : sourceKey.toCharArray()) {
                key = String.valueOf(c);
                if (key.matches("^\\w+$") && !keyMap.containsKey(key.toLowerCase())) {
                    keyMap.put(key.toLowerCase(), elem);
                    return key;
                }
                index++;
            }
            return null;
        };

        actions.forEach(it -> findValidKey.apply(it.getTextContent(), it));
        fixed.forEach(it -> findValidKey.apply(it.getTextContent(), it));
        for (Element elem : buttons) {
            assert !elem.getAttribute("id").isEmpty();
            String sourceKey = elem.getTextContent();
            var key = findValidKey.apply(sourceKey, elem);
            if (!elem.hasAttribute("keyboard") && key != null && !selected.contains(elem)) {
                var index = sourceKey.toLowerCase().indexOf(key.toLowerCase());
                elem.setTextContent("");
                Text pre = engine.getDocument().createTextNode("[" + sourceKey.substring(0, index));
                Element tKey = engine.getDocument().createElement("u");
                tKey.appendChild(engine.getDocument().createTextNode(key));
                Element bold = engine.getDocument().createElement("b");
                bold.appendChild(tKey);
                Text post = engine.getDocument().createTextNode(sourceKey.substring(index + 1) + "]");
                elem.appendChild(pre);
                elem.appendChild(bold);
                elem.appendChild(post);
//                elem.setTextContent("[" + sourceKey.substring(0, index) + "<strong>" + key + "</strong>" + sourceKey.substring(index + 1) + "]");
                elem.setAttribute("keyboard", String.valueOf(true));

                String finalKey = key;
            }
        }

        if (keyboardHandler != null) {
            view.removeEventFilter(KeyEvent.KEY_RELEASED, keyboardHandler);
        }
        keyboardHandler = event -> {
            var tagName = engine.executeScript("document.activeElement.tagName").toString();
            var pressed = event.getText().toLowerCase();
            if (!Objects.equals(tagName, "INPUT")) {
                event.consume();
                for (String key : keyMap.keySet()) {
                    if (pressed.equals(key)) {
                        var id = keyMap.get(key).getAttribute("id");
                        var jObj = (JSObject) callJSFunction(engine, "document.getElementById", id);
                        try {
                            jObj.call("click");
                        } catch (NullPointerException e) {
                            System.err.println("with id " + id);
                            throw e;
                        }
                    }
                }
            }
        };
        keyboardReleaseHandler = event -> {
            var tagName = engine.executeScript("document.activeElement.tagName").toString();
            event.consume();
        };
        view.addEventFilter(KeyEvent.KEY_RELEASED, keyboardHandler);
    }

}
