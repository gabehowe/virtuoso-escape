package org.virtuoso.escape.gui;

// JEP 511

import module javafx.fxml;
import module javafx.graphics;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.virtuoso.escape.model.GameProjection;

import java.io.IOException;

public class EscapeApplication extends Application {

    private static Scene scene;
    public static GameProjection projection;

    @Override
    public void start(Stage stage) throws IOException {
        projection = new GameProjection();
        FXMLLoader loginLoader = new FXMLLoader(EscapeApplication.class.getResource("login-view.fxml"));
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
        FXMLLoader fxmlLoader = new FXMLLoader(EscapeApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void setApp(WebEngine engine, Object app) {
        engine.documentProperty().addListener((obs, old, newDoc) -> {
            System.out.println("Succeded!");
            var window = (JSObject) engine.executeScript("window");
            window.setMember("app", app);
            engine.executeScript("console.log = function(message) { app.log(message); };" + "console.error = function(message) { app.error(message); };");

        });
    }

    public static void setText(WebEngine engine, String elementId, String text) {
        engine.executeScript(String.format("document.getElementById('%s').textContent='%s';", elementId, text));
    }

}
