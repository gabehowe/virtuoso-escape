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
            exit();
        });
    }

    public static void exit() {
        Platform.exit();
        System.exit(0);
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

    private static final Logger logger = new Logger();

    public static void setApp(WebEngine engine, Object app, Runnable callback) {
        engine.documentProperty().addListener((_, _, _) -> {
            var window = (JSObject) engine.executeScript("window");
            window.setMember("app", app);
            window.setMember("logger", logger);
            callback.run();
        });
    }

    public static void setText(WebEngine engine, String elementId, String text) {
        var sanitized = text.replace("\"", "\\\"")
                            .replace("\n", "<br>").replace("\t", "<span class='tab'></span>");
        callJSFunction(engine, "setTextOnElement", elementId, sanitized);
    }

    public static class Logger {
        private static final boolean LOGJSCALLS = false;

        public static void logJSCall(String msg) {
            if (LOGJSCALLS) log("[JS Call]: " + msg);
        }

        public static void log(Object msg) {
            System.out.println(msg);
        }

        public void log_(Object msg) {
            log(msg);
        }

        public void error(Object msg) {
            System.err.print("JSError: " + msg);
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
        Logger.logJSCall(cmd);
        return engine.executeScript(cmd);
    }

    public static EventHandler<KeyEvent> keyboardHandler;

}
