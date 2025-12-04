package org.virtuoso.escape.gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.virtuoso.escape.speech.SpeechPlayer;

public class IntroController {

    @FXML
    private ImageView barn;

    @FXML
    private ImageView openedBarn;

    @FXML
    private ImageView beaver;

    @FXML
    private AnchorPane blackScreen;

    @FXML
    private AnchorPane root;

    @FXML
    private Button continueToGame;

    @FXML
    private Label introLabel;

    @FXML
    private Label barnLabel;

    @FXML
    private Label beaverLabel;

    @FXML
    private Label pressToSkipDialogue;

    @FXML
    private Label pressToSkipIntro;

    private PauseTransition typewriter;
    private boolean wasScreenClicked;
    private String introLabelText;
    private static final double typewriteDelay = 0.045;

    @FXML
    void initialize() {
        App.scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.Q) App.loadWebView(new GameViewController());
            SpeechPlayer.instance().stopSoundbite();
        });

        pressToSkipIntro.setOpacity(0);
        pressToSkipIntro.setVisible(true);

        pressToSkipAnimation(pressToSkipIntro, 0.5, 0.45);

        barn.setVisible(true);
        barn.setDisable(true);
        barn.setPickOnBounds(false);

        FadeTransition fadeInFG = new FadeTransition(Duration.seconds(0.3), barn);
        fadeInFG.setOnFinished(event -> barn.setDisable(false));
        fadeInFG.setFromValue(0);
        fadeInFG.setToValue(1);

        FadeTransition fadeInBG = new FadeTransition(Duration.seconds(0.26), root);
        fadeInBG.setFromValue(0);
        fadeInBG.setToValue(1);

        fadeInFG.play();
        fadeInBG.play();

        openedBarn.setVisible(false);
        openedBarn.setDisable(true);

        beaver.setOpacity(0);
        beaver.setVisible(true);
        beaver.setDisable(true);
        beaver.setPickOnBounds(false);

        blackScreen.setOpacity(0);
        blackScreen.setVisible(true);
        blackScreen.setMouseTransparent(true);

        continueToGame.setVisible(false);
        continueToGame.setDisable(true);

        introLabel.getParent().setMouseTransparent(true);
        introLabel.setVisible(false);
        introLabelText = introLabel.getText();

        barnLabel.setVisible(false);
        beaverLabel.setVisible(false);

        pressToSkipDialogue.setOpacity(0);
        pressToSkipDialogue.setVisible(true);
    }

    @FXML
    void onContinueButtonClick() {
        App.loadWebView(new GameViewController());
        SpeechPlayer.instance().stopSoundbite();
    }

    @FXML
    void onBarnClick() {
        barnLabel.setVisible(false);
        barn.setVisible(false);
        barn.setDisable(true);
        openedBarn.setVisible(true);

        FadeTransition fadeInBeaver = new FadeTransition(Duration.seconds(0.3), beaver);
        fadeInBeaver.setToValue(1);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.73));
        pause.setOnFinished(event -> {
            beaver.setDisable(false);
            fadeInBeaver.play();
        });
        pause.play();
    }

    @FXML
    void onBeaverClick() {
        beaver.setVisible(false);
        beaver.setDisable(true);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), blackScreen);
        fadeIn.setToValue(1);

        fadeIn.setOnFinished(event -> {
            startTypewriteAnimation(introLabel, introLabel.getText());
            openedBarn.setVisible(false);
            blackScreen.setMouseTransparent(false);
            introLabel.setVisible(true);
            pressToSkipAnimation(pressToSkipDialogue, 0.5, 0.43);
        });
        fadeIn.play();
    }

    @FXML
    void onBarnEnter() {
        barnLabel.setVisible(true);
    }

    @FXML
    void onBarnExit() {
        barnLabel.setVisible(false);
    }

    @FXML
    void onBeaverEnter() {
        beaverLabel.setVisible(true);
    }

    @FXML
    void onBeaverExit() {
        beaver.setOpacity(1);
        beaverLabel.setVisible(false);
    }

    void pressToSkipAnimation(Label label, double fadeTime, double pauseTime) {
        FadeTransition fadeInPTS = new FadeTransition(Duration.seconds(fadeTime), label);
        fadeInPTS.setToValue(1);

        FadeTransition fadeOutPTS = new FadeTransition(Duration.seconds(fadeTime), label);
        fadeOutPTS.setToValue(0);

        PauseTransition pausePTS1 = new PauseTransition(Duration.seconds(pauseTime));
        PauseTransition pausePTS2 = new PauseTransition(Duration.seconds(pauseTime));

        fadeInPTS.setOnFinished(event -> pausePTS1.play());
        pausePTS1.setOnFinished(event -> fadeOutPTS.play());
        fadeOutPTS.setOnFinished(event -> pausePTS2.play());
        pausePTS2.setOnFinished(event -> fadeInPTS.play());

        fadeInPTS.play();
    }

    void startTypewriteAnimation(Label label, String text) {
        label.setText("");
        typewriterAnimation(label, text, 0);
        SpeechPlayer.instance().playSoundbite(text);
    }

    @FXML
    void skipTypewriteAnimation() {
        if (wasScreenClicked) return;
        wasScreenClicked = true;

        if (typewriter != null) {
            typewriter.stop();
            SpeechPlayer.instance().stopSoundbite();
        }
        introLabel.setText(introLabelText);
        pressToSkipDialogue.setVisible(false);
        continueToGame.setDisable(false);
        continueToGame.setVisible(true);
    }

    private void typewriterAnimation(Label label, String text, int index) {
        if (index == text.length()) {
            wasScreenClicked = true;
            pressToSkipDialogue.setVisible(false);
            continueToGame.setDisable(false);
            continueToGame.setVisible(true);
            return;
        }
        label.setText(label.getText() + text.charAt(index));

        typewriter = new PauseTransition(Duration.seconds(typewriteDelay));
        // Unused used here instead of "_" because of linter bug
        typewriter.setOnFinished(unused -> typewriterAnimation(label, text, index + 1));
        typewriter.play();
    }
}
