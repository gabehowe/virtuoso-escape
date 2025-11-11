package org.virtuoso.escape.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.account.AccountManager;


import java.io.IOException;

public class LoginController {
    LoginController(GameProjection projection) {
        this.proj = projection;
    }

    @FXML
    public TextField usernameEntry;
    @FXML
    public PasswordField passwordEntry;
    GameProjection proj;

    enum AuthMode {
        LOGIN,
        CREATE
    }

    private AuthMode authMode = AuthMode.LOGIN;
    @FXML
    private Label promptAuthMode;
    @FXML
    private Label toggleAuthMode;
    @FXML
    private Label welcomeText;
    @FXML
    private Pane root;

    void switchToGame() {
        try {
			EscapeApplication.setRoot("game-view");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    void displayErrorMessage(String message) {
        var error = (Label) root.lookup(".error");
        if (error != null) {
            error.setText(message);
            return;
        }
        error = new Label(message);
        error.getStyleClass().add("error");
        root.getChildren().add(error);
    }


    @FXML
    void tryAuth() {
        System.out.println(this.authMode);
        String user = usernameEntry.getText();
        String pass = passwordEntry.getText();
        var flag = switch (authMode) {
            case LOGIN -> proj.login(user, pass);
            case CREATE -> proj.createAccount(user, pass);
        };
        if (flag) {
            // Move to next screen
            switchToGame();
        } else {
            displayErrorMessage(AccountManager.instance().invalidLoginInfo(usernameEntry.getText(), passwordEntry.getText()));
        }
    }

    @FXML
    void initialize() {
        KeyboardProcessor.addKeyboardBindings(root);
    }

    @FXML
    private void toggleAuthMode() {
        switch (this.authMode) {
            case LOGIN -> {
                promptAuthMode.setText(GameInfo.instance().string("ui", "switch_login"));
                toggleAuthMode.setText(GameInfo.instance().string("ui", "prompt_login"));
                welcomeText.setText(GameInfo.instance().string("ui", "prompt_create"));
                this.authMode = AuthMode.CREATE;
            }
            case CREATE -> {
                promptAuthMode.setText(GameInfo.instance().string("ui", "switch_create"));
                toggleAuthMode.setText(GameInfo.instance().string("ui", "prompt_create"));
                welcomeText.setText(GameInfo.instance().string("ui", "prompt_login"));
                this.authMode = AuthMode.LOGIN;
            }
        }
        KeyboardProcessor.addKeyboardBindings(root);
    }

}
