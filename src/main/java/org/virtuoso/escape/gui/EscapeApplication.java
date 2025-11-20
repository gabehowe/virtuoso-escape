package org.virtuoso.escape.gui;

// JEP 511

import module javafx.fxml;
import module javafx.graphics;
import javafx.event.EventHandler;

import org.virtuoso.escape.model.GameProjection;

import java.io.IOException;
import java.util.stream.Collectors;

public class EscapeApplication extends Application {

	private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        GameProjection projection = new GameProjection();
        FXMLLoader loginLoader = new FXMLLoader(EscapeApplication.class.getResource("login-view.fxml"));
        loginLoader.setController(new LoginController(projection));
        scene = new Scene(loginLoader.load(), 700, 475);
        stage.setTitle("Virtuoso Escape");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(t -> {
            // TODO Logout
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


}
