package org.virtuoso.escape.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.virtuoso.escape.model.GameProjection;

public class GameViewController {
    GameProjection projection;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
