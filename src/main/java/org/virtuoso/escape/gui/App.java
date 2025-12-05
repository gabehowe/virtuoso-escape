package org.virtuoso.escape.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.w3c.dom.Element;

/**
 * A JavaFX GUI for Virtuoso Escape.
 * @author gabri
 * @author Andrew
 */
public class App extends Application {
    public static Scene scene;
    public static GameProjection projection;
    public static CurrentPage page;
    public static final Logger logger = new Logger();


    /**
     * Begin the application.
     * @param stage The initial stage.
     */
    @Override
    public void start(Stage stage) {
        page = CurrentPage.NONE;
        projection = new GameProjection();
        App.loadWebView(new LoginController());
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();
        // Unused used here instead of "_" because of linter bug
        stage.setOnCloseRequest(unused -> exit());
    }

    /**
     * Close the application.
     */
    public static void exit() {
        if (GameState.instance().account() != null)
            App.projection.logout();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Set the root based on an FXML file.
     * @param fxml The name of the fxml file (without the file extension).
     * @throws IOException If the file does not exist.
     */
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
    }

    /**
     * Run the application.
     */
    static void main() {
        launch();
    }

    /**
     * Set the global constant app in the webengine.
     * @param engine The {@link WebEngine} under which to set the constant.
     * @param app The value to set the constant to.
     * @param callback The lambda to run when the document finishes loading.
     */
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

    /**
     * Create a simple web view with a controller.
     * @param controller The controller to attach to the web view.
     */
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

    /**
     * Set text in an element.
     * @param engine The {@link WebEngine} under which to set the text.
     * @param elementId The ID of the element in which to set the text.
     * @param text The text to set.
     */
    public static void setText(WebEngine engine, String elementId, String text) {
        engine.executeScript(
                String.format(" document.getElementById('%s').innerHTML = \"%s\"", elementId, sanitizeForJS(text)));
    }

    /**
     * Replace near markdownizations with HTML friendly representations.
     * @apiNote
     * Supports replacing
     *<ul>
     *     <li>{@code >/<} with {@code &gt;/&lt;} </li>
     *     <li>{@code \n} with {@code <br>}</li>
     *     <li>{@code \t} with {@code <span class='tab'></span>}</li>
     *     <li>{@code *text*} with {@code <em>text</em>}</li>
     *     <li>{@code **text**} with {@code <strong>text</strong>}</li>
     *</ul>
     * @param text The markdown-like text to santitize.
     * @return The sanitized HTML ready text.
     */
    public static String sanitizeForJS(String text) {
        return text.replace("\"", "\\\"")
                .replace(">", "&gt;")
                .replace("<", "&lt;")
                .replace("\n", "<br>")
                .replace("\t", "<span class='tab'></span>")
                .replaceAll("(?<!\\*)\\*([^*]+?)\\*(?!\\*)", "<em>$1</em>")
                .replaceAll("\\*\\*([^*]+?)\\*\\*", "<strong>$1</strong>");
    }

    /**
     * Find all elements that satisfy the css selector.
     * @param engine The engine under which to search.
     * @param selector The CSS selector to search by.
     * @return A list of elements.
     */
    public static ArrayList<Element> querySelectorAll(WebEngine engine, String selector) {
        var elements = (Integer) engine.executeScript("j = document.querySelectorAll('" + selector + "'); j.length");
        var list = new ArrayList<Element>();
        for (int i = 0; i < elements; i++) {
            list.add((Element) engine.executeScript("j[" + i + "]"));
        }
        return list;
    }

    /**
     * Call an arbitrary javascript function.<br>
     * Example:
     * <pre>{@code
     * App.callJSFunction(engine, "updateBox", id, current, mapped, button);
     * }</pre>
     * @param engine The {@link WebEngine} under which to call the functino.
     * @param function The function name to call.
     * @param args Args to provide to the function.
     * @return The result of the function call.
     */
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

    /**
     * A simple logger class to support logging from javascript.
     * @author gabri
     */
    public static class Logger {
        private static final boolean LOGJSCALLS = true;
        private static final boolean LOGJS = true;

        /**
         * Log a call to javascript from java.
         * @param msg The message to log.
         */
        public void logJSCall(String msg) {
            if (LOGJSCALLS) log("[JS Call]: " + msg);
        }

        /**
         * Log a message.
         * @apiNote Should be called from javascript.
         * @param msg The message to log.
         */
        public void logJS(String msg) {
            if (LOGJS) log("[JS]: " + msg);
        }

        /**
         * Log a message.
         * @param msg The message to log.
         */
        public void log(Object msg) {
            System.out.println(msg);
        }

        /**
         * Log an error.
         * @param msg The error to log.
         */
        public void error(String msg) {
            System.err.println(msg);
        }

        /**
         * Log an error.
         * @apiNote Should be called from javascript.
         * @param msg The error to log.
         */
        public void logJSError(Object msg) {
            error("JSError: " + msg);
            if (msg instanceof Exception e) {
                e.printStackTrace();
            }
        }
    }
}
