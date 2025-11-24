package org.virtuoso.escape.gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.virtuoso.escape.speech.SpeechPlayer;

import java.io.IOException;

public class IntroController {

	@FXML
	private ImageView barn;
	@FXML
	private ImageView hoveredBarn;
	@FXML
	private ImageView openedBarn;
	@FXML
	private ImageView beaver;
	@FXML
	private ImageView background;
	@FXML
	private Pane blackScreen;
	@FXML
	private Button continueToGame;
	@FXML
	private Label introLabel;
	@FXML
	private Label barnLabel;
	@FXML
	private Label beaverLabel;
	@FXML
	private Label pressToSkip;

	private PauseTransition typewriter;
	private boolean isBarnOpen;
	private boolean wasScreenClicked;
	private String introLabelText;

	@FXML
	void initialize() {
		barn.setVisible(true);
		barn.setDisable(true);

		FadeTransition fadeInBarn = new FadeTransition(Duration.seconds(0.3), barn);
		fadeInBarn.setFromValue(0);
		fadeInBarn.setToValue(1);
		fadeInBarn.setOnFinished(event -> barn.setDisable(false));

		FadeTransition fadeInBG = new FadeTransition(Duration.seconds(0.3), background);
		fadeInBG.setFromValue(0);
		fadeInBG.setToValue(1);

		fadeInBG.play();
		fadeInBarn.play();

		hoveredBarn.setVisible(false);

		openedBarn.setVisible(false);
		openedBarn.setDisable(true);

		beaver.setOpacity(0);
		beaver.setVisible(true);
		beaver.setDisable(true);

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

		pressToSkip.setOpacity(0);
		pressToSkip.setVisible(true);
	}

	@FXML
	void onContinueButtonClick() {
		try {
			App.setRoot("game-view");
			SpeechPlayer.instance().stopSoundbite();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@FXML
	void onBarnEnter() {
		barn.setVisible(false);
		hoveredBarn.setVisible(true);
		barnLabel.setVisible(true);
	}

	@FXML
	void onHBarnClick() {
		isBarnOpen = true;

		barn.setDisable(true);
		barnLabel.setVisible(false);

		hoveredBarn.setVisible(false);
		hoveredBarn.setDisable(true);
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
	void onHBarnPressed() {
		hoveredBarn.setScaleX(openedBarn.getScaleX());
		hoveredBarn.setScaleY(openedBarn.getScaleY());
	}

	@FXML
	void onHBarnExit() {
		if (isBarnOpen) return;
		hoveredBarn.setVisible(false);
		barnLabel.setVisible(false);
		barn.setVisible(true);
	}

	@FXML
	void onBeaverClick() {
		beaver.setVisible(false);
		beaver.setDisable(true);

		FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), blackScreen);
		fadeIn.setToValue(1);

		fadeIn.setOnFinished(event -> {
			startTypewriteAnimation(introLabel, introLabel.getText(), 0.045);
			openedBarn.setVisible(false);
			blackScreen.setMouseTransparent(false);
			introLabel.setVisible(true);
			fadeInPauseFadeOut(0.5, 0.4);
		});
		fadeIn.play();
	}

	@FXML
	void onBeaverEnter() {
		beaver.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 10, 0, 0, 0));
		beaver.setRotate(6);
		beaver.setOpacity(0.98);
		beaver.setScaleX(1.01);
		beaver.setScaleY(1.01);
		beaverLabel.setVisible(true);
	}

	@FXML
	void onBeaverExit() {
		beaver.setEffect(new InnerShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 10, 0, 0, 0));
		beaver.setRotate(0);
		beaver.setOpacity(1);
		beaverLabel.setVisible(false);
	}

	@FXML
	void onContinueButtonEnter() {
		continueToGame.setScaleX(1.03);
		continueToGame.setScaleY(1.03);
	}

	@FXML
	void onContinueButtonExit() {
		continueToGame.setScaleX(1);
		continueToGame.setScaleY(1);
	}

	void fadeInPauseFadeOut(double fadeTime, double pauseTime) {
		FadeTransition fadeInPTS = new FadeTransition(Duration.seconds(fadeTime), pressToSkip);
		fadeInPTS.setToValue(1);

		FadeTransition fadeOutPTS = new FadeTransition(Duration.seconds(fadeTime), pressToSkip);
		fadeOutPTS.setToValue(0);

		PauseTransition pausePTS1 = new PauseTransition(Duration.seconds(pauseTime));
		PauseTransition pausePTS2 = new PauseTransition(Duration.seconds(pauseTime));

		fadeInPTS.setOnFinished(event -> pausePTS1.play());
		pausePTS1.setOnFinished(event -> fadeOutPTS.play());
		fadeOutPTS.setOnFinished(event -> pausePTS2.play());
		pausePTS2.setOnFinished(event -> fadeInPTS.play());

		fadeInPTS.play();
	}

	void startTypewriteAnimation(Label label, String text, double delay) {
		label.setText("");
		typewriter(label, text, 0, delay);
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
		pressToSkip.setVisible(false);
		continueToGame.setDisable(false);
		continueToGame.setVisible(true);
	}

	private void typewriter(Label label, String text, int index, double delay) {
		if (index == text.length()) {
			pressToSkip.setVisible(false);
			continueToGame.setDisable(false);
			continueToGame.setVisible(true);
			return;
		}
		label.setText(label.getText() + text.charAt(index));

		typewriter = new PauseTransition(Duration.seconds(delay));
		typewriter.setOnFinished(event -> typewriter(label, text, index + 1, delay));
		typewriter.play();
	}
}