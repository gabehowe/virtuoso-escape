package org.virtuoso.escape.gui;

// JEP 511

import module javafx.fxml;
import module javafx.graphics;

import org.virtuoso.escape.model.GameProjection;

import java.io.IOException;

public class EscapeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameProjection projection = new GameProjection();
        FXMLLoader fxmlLoader = new FXMLLoader(EscapeApplication.class.getResource("game-view.fxml"));
        FXMLLoader loginLoader = new FXMLLoader(EscapeApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loginLoader.load(), 700, 475);
        LoginController controller = loginLoader.getController();
        controller.projection = projection;
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();
    }
}
