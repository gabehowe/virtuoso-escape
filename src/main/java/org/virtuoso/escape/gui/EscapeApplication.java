package org.virtuoso.escape.gui;

// JEP 511

import module javafx.fxml;
import module javafx.graphics;

import org.virtuoso.escape.model.GameProjection;

import java.io.IOException;

public class EscapeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        System.out.print(Font.getFamilies());
        GameProjection projection = new GameProjection();
        FXMLLoader loginLoader = new FXMLLoader(EscapeApplication.class.getResource("login-view.fxml"));
        loginLoader.setController(new LoginController(projection, stage));
        Scene scene = new Scene(loginLoader.load(), 700, 475);
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();
    }


}
