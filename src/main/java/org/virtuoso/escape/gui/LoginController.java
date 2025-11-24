package org.virtuoso.escape.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.account.AccountManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    LoginController(GameProjection projection) {
        this.proj = projection;
    }

    @FXML
    public TextField usernameEntry;
    @FXML
    public PasswordField passwordEntry;
    GameProjection proj;
    @FXML
    public WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        KeyboardProcessor.addKeyboardBindings(root);
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("login.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> {});
    }

    enum AuthMode {
        LOGIN,
        CREATE
    }

    private AuthMode authMode = AuthMode.LOGIN;

	void switchToIntro() {
        try {
            App.setRoot("intro-view");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String tryAuth(String user, String pass) {
        var flag = switch (authMode) {
            case LOGIN -> proj.login(user, pass);
            case CREATE -> proj.createAccount(user, pass);
        };
        if (flag) {
            // Move to next screen
	        switchToIntro();
        } else {
            return AccountManager.instance().invalidLoginInfo(user, pass);
        }
        return "";
    }

    public void toggleAuthMode() {
        switch (this.authMode) {
            case LOGIN -> {
                App.setText(webView.getEngine(), "auth-prompt", GameInfo.instance().string("ui", "switch_login"));
                App.setText(webView.getEngine(), "auth-change", GameInfo.instance().string("ui", "prompt_login"));
                App.setText(webView.getEngine(), "welcome-text", GameInfo.instance().string("ui", "prompt_create"));
                this.authMode = AuthMode.CREATE;
            }
            case CREATE -> {
                App.setText(webView.getEngine(), "auth-prompt", GameInfo.instance().string("ui", "switch_create"));
                App.setText(webView.getEngine(), "auth-change", GameInfo.instance().string("ui", "prompt_create"));
                App.setText(webView.getEngine(), "welcome-text",GameInfo.instance().string("ui", "prompt_login"));
                this.authMode = AuthMode.LOGIN;
            }
        }
//        KeyboardProcessor.addKeyboardBindings(root);
    }

}
