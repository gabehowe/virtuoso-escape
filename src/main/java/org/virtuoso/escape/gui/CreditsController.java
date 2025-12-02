package org.virtuoso.escape.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;

public class CreditsController implements Initializable {
    CreditsController(GameProjection projection) {
        this.proj = projection;
    }

    GameProjection proj;

    @FXML
    public WebView webView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("credits.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> App.callJSFunction(webView.getEngine(), "updateKeyHandler", "c"));
    }

    /** Switch to the next screen: intro for new users or game for returning users. */
    void switchToNextScreen() {
        if (GameState.instance().time().toSeconds() == GameState.initialTime) {
            try {
                App.setRoot("intro-view");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                App.setRoot("game-view");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
