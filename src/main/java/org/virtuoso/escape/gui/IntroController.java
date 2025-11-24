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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.virtuoso.escape.speech.SpeechPlayer;

import java.io.IOException;

public class IntroController {

	public StackPane foreground;
	public VBox beaverBox;
	@FXML
	private ImageView hoveredBarn;
	@FXML
	private ImageView openedBarn;
	@FXML
	private ImageView beaver;
	@FXML
	private AnchorPane blackScreen;
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
	private double typewriteDelay = 1.0/60;

	@FXML
	void initialize() {
//		hoveredBarn.setVisible(false);
//		barn.setVisible(true);

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
		barnLabel.setVisible(true);
	}

	@FXML
	void onHBarnClick() {
		isBarnOpen = true;

		barnLabel.setVisible(false);

		hoveredBarn.setVisible(false);
		hoveredBarn.setDisable(true);
		openedBarn.setVisible(true);

		FadeTransition fadeInBeaver = new FadeTransition(Duration.seconds(0.3), beaver);
		fadeInBeaver.setToValue(1);
		PauseTransition pause = new PauseTransition(Duration.seconds(1.73));
		pause.setOnFinished(event -> {
			beaver.setDisable(false);
			beaverBox.setMouseTransparent(false);
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
		barnLabel.setVisible(false);
	}

	@FXML
	void onBeaverClick() {
		beaver.setVisible(false);
		beaver.setDisable(true);

		FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), blackScreen);
		fadeIn.setToValue(1);

		fadeIn.setOnFinished(event -> {
			startTypewriteAnimation(introLabel, introLabel.getText() );
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
		beaverLabel.setVisible(true);
	}

	@FXML
	void onBeaverExit() {
		beaver.setEffect(new InnerShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 10, 0, 0, 0));
		beaver.setRotate(0);
		beaver.setOpacity(1);
		beaverLabel.setVisible(false);
	}
//
//	@FXML
//	void onContinueButtonEnter() {
//		continueToGame.setStyle("-fx-font-size: 17.5px;");
//		continueToGame.setScaleX(1.03);
//		continueToGame.setScaleY(1.03);
//	}
//
//	@FXML
//	void onContinueButtonExit() {
//		continueToGame.setStyle("-fx-font-size: 17px;");
//		continueToGame.setScaleX(1);
//		continueToGame.setScaleY(1);
//	}

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

	void startTypewriteAnimation(Label label, String text) {
		label.setText("");
		typewriter(label, text, 0);
		SpeechPlayer.instance().playSoundbite(text);
	}

	@FXML
	void skipTypewriteAnimation() {
		if (wasScreenClicked) return;
		wasScreenClicked = true;

		if (typewriter != null) {
			SpeechPlayer.instance().stopSoundbite();
		}
		typewriteDelay /= 200;
		pressToSkip.setVisible(false);
		continueToGame.setDisable(false);
		continueToGame.setVisible(true);
	}

	private void typewriter(Label label, String text, int index) {
		if (index == text.length()) {
			pressToSkip.setVisible(false);
			continueToGame.setDisable(false);
			continueToGame.setVisible(true);
			return;
		}
		label.setText(label.getText() + text.charAt(index));

		typewriter = new PauseTransition(Duration.seconds(typewriteDelay));
		typewriter.setOnFinished(_ -> typewriter(label, text, index + 1));
		typewriter.play();
	}
}