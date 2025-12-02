package org.virtuoso.escape.gui;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.Leaderboard;
import org.virtuoso.escape.model.account.Score;

/**
 * Controller for the credits and leaderboard screen. Interfaces with WebView javascript engine.
 *
 * @author aheuer
 */
public class CreditsController implements Initializable {
    private final Leaderboard leaderboard = new Leaderboard();
    public GameProjection projection;

    @FXML
    public WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(getClass().getResource("credits.html").toExternalForm());
        App.setApp(webView.getEngine(), this, () -> App.callJSFunction(webView.getEngine(), "updateKeyHandler", "c"));
    }

    /**
     * Called in javascript to return the current run info, serializes the string as tab separated values. Records the
     * session to the leaderboard.
     *
     * @return A string array with all current run info.
     */
    public String[] getRunInfo() {
        Account currentAccount = GameState.instance().account();

        String usernameToRecord = (currentAccount != null) ? currentAccount.username() : "Guest";

        leaderboard.recordSession(usernameToRecord);

        String formattedTime = String.format(
                "%02d:%02d",
                GameState.instance().time().toMinutesPart(),
                GameState.instance().time().toSecondsPart());

        long totalScore = Score.calculateScore(
                GameState.instance().time(), GameState.instance().difficulty());

        Map<String, Integer> hintsUsedMap = GameState.instance().hintsUsed();
        int totalHintsUsed = hintsUsedMap.values().stream().reduce(0, Integer::sum);

        return new String[] {
            formattedTime,
            String.valueOf(totalScore),
            String.valueOf(totalHintsUsed),
            GameState.instance().difficulty().toString()
        };
    }

    /**
     * Called in javascript to return the leaderboard contents.
     *
     * @return A string array with all leaderboard values.
     */
    public String[] getLeaderboardElements() {
        List<String> lb_array = leaderboard.getLeaderboard();
        return lb_array.toArray(new String[0]);
    }
}
