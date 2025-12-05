package org.virtuoso.escape.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.account.AccountManager;

public class LoginController implements Initializable {

    @FXML
    public WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        App.page = App.page.LOGIN;
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("login.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> App.callJSFunction(webView.getEngine(), "updateKeyHandler", "c"));
        webView.setContextMenuEnabled(false);
    }

    /** All possible authentication states. */
    enum AuthMode {
        LOGIN,
        CREATE
    }

    private AuthMode authMode = AuthMode.LOGIN;

    /** Switch to the next screen: intro for new users or game for returning users. */
    void switchToNextScreen() {
        if (GameState.instance().time().toSeconds() == GameState.initialTime) {
            try {
                App.setRoot("intro-view");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            App.loadWebView(new GameViewController());
        }
    }

    /**
     * Attempt to authenticate and return the error or move to the next screen.
     *
     * @param user The username to attempt.
     * @param pass The password to attempt.
     * @return A string error message if the message fails, else nothing.
     */
    public String tryAuth(String user, String pass) {
        try {

            var flag =
                    switch (authMode) {
                        case LOGIN -> App.projection.login(user, pass);
                        case CREATE -> App.projection.createAccount(user, pass);
                    };

            if (flag) {
                // Move to next screen
                switchToNextScreen();
            } else {
                return AccountManager.instance().invalidLoginInfo(user, pass);
            }
        } catch (Exception e) {
            App.logger.error(e.toString());
            throw new RuntimeException(e);
        }
        return "Bad State!";
    }

    /**
     * Toggle the auth mode between {@link AuthMode}s
     *
     * @return a String[3] of the prompt text, the change button text, and the welcome message text.
     */
    public String[] toggleAuthMode() {
        return switch (this.authMode) {
            case LOGIN -> {
                this.authMode = AuthMode.CREATE;
                yield new String[] {
                    GameInfo.instance().string("ui", "switch_login"),
                    GameInfo.instance().string("ui", "prompt_login"),
                    GameInfo.instance().string("ui", "prompt_create")
                };
            }
            case CREATE -> {
                this.authMode = AuthMode.LOGIN;
                yield new String[] {
                    GameInfo.instance().string("ui", "switch_create"),
                    GameInfo.instance().string("ui", "prompt_create"),
                    GameInfo.instance().string("ui", "prompt_login")
                };
            }
        };
    }

    /** Close the application. */
    public void exit() {
        App.exit();
    }

}
