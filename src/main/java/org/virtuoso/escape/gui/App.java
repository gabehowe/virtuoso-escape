package org.virtuoso.escape.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.w3c.dom.Element;

public class App extends Application {
    public static Scene scene;
    public static GameProjection projection;
    public static CurrentPage page;

    @Override
    public void start(Stage stage) throws IOException {
        page = page.NONE;
        projection = new GameProjection();
        App.loadWebView(new LoginController());
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();
        // Unused used here instead of "_" because of linter bug
        stage.setOnCloseRequest(unused -> exit());
    }

    public static void exit() {
        if (GameState.instance().account() != null)
            App.projection.logout();
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

    public static final Logger logger = new Logger();

    public static void setApp(WebEngine engine, Object app, Runnable callback) {
        // Unused used here instead of "_" because of linter bug
        engine.documentProperty().addListener((unused1, unused2, unused3) -> {
            var window = (JSObject) engine.executeScript("window");
            window.setMember("app", app);
            window.setMember("logger", logger);
            engine.executeScript(
                    """
                    console.error = i => logger.logJSError(i);
                    console.log = i => logger.logJS(i);
                    window.onerror = e => console.error(e);
                    """);
            callback.run();
        });
    }

    public static void loadWebView(Initializable controller) {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("web-view.fxml"));
        loader.setController(controller);
        try {
            if (App.scene != null) App.scene.setRoot(loader.load());
            else App.scene = new Scene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setText(WebEngine engine, String elementId, String text) {
        engine.executeScript(
                String.format(" document.getElementById('%s').innerHTML = \"%s\"", elementId, sanitizeForJS(text)));
    }

    public static String sanitizeForJS(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "<br>")
                .replace("\t", "<span class='tab'></span>")
                .replaceAll("(?<!\\*)\\*([^*]+?)\\*(?!\\*)", "<em>$1</em>")
                .replaceAll("\\*\\*([^*]+?)\\*\\*", "<strong>$1</strong>");
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
        var jsArgs = Arrays.stream(args)
                .map(it -> {
                    if (it instanceof String j) {
                        return "\"" + j + "\"";
                    }
                    return it;
                })
                .map(Object::toString)
                .collect(Collectors.joining(","));
        var cmd = String.format("%s(%s);", function, jsArgs);
        logger.logJSCall(cmd);
        return engine.executeScript(cmd);
    }

    public static class Logger {
        private static final boolean LOGJSCALLS = true;
        private static final boolean LOGJS = true;

        public void logJSCall(String msg) {
            if (LOGJSCALLS) log("[JS Call]: " + msg);
        }

        public void logJS(String msg) {
            if (LOGJS) log("[JS]: " + msg);
        }

        public void log(Object msg) {
            System.out.println(msg);
        }

        public void error(String msg) {
            System.err.println(msg);
        }

        public void logJSError(Object msg) {
            error("JSError: " + msg);
            if (msg instanceof Exception e) {
                e.printStackTrace();
            }
        }
    }
}
