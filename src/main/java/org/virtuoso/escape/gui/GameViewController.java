package org.virtuoso.escape.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.virtuoso.escape.model.GameProjection;

import java.net.URL;
import java.util.ResourceBundle;

public class GameViewController implements Initializable {
    GameProjection projection;
    @FXML
    private Pane root;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        KeyboardProcessor.addKeyboardBindings(this.root);
    }
}
