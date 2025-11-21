package org.virtuoso.escape.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import org.virtuoso.escape.model.GameProjection;

import java.net.URL;
import java.util.ResourceBundle;

public class GameViewController implements Initializable {
    GameProjection projection;
    @FXML
    private Pane root;
    @FXML
    private WebView webView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().load(getClass().getResource("game-view.html").toExternalForm());
        EscapeApplication.setApp(webView.getEngine(), this);
    }
}
