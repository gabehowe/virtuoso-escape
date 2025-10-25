package org.virtuoso.escape.model.account;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Leaderboard
 * Displays high scores per user accounts
 */

public class Leaderboard {

	/**
	 * Update the user's high score.
	 * @param username The user's username.
	 */
    public void recordSession(String username) {
        GameState state = GameState.instance();
        Account account = state.account();

        if (account != null) {
            Score newScore = new Score(state.time(), state.difficulty());
            Score currentHighScore = account.highScore();

            if (newScore.totalScore() > currentHighScore.totalScore()) {
                account.setHighScore(newScore);
                System.out.println("Score updated");
            }
        }
    }

	/**
	 * Print the leaderboard of scores and difficulties.
	 */
    public void showLeaderboard() {
        JSONObject accountsJson = AccountManager.instance().accounts();
        List<ScoreEntry> allScores = new ArrayList<>();

        for (Object id : accountsJson.keySet()) {
            Object value = accountsJson.get(id);
            if (value instanceof JSONObject acct) {
                String username = acct.get("username").toString();
                Object highScoreObj = acct.get("highScore");
                if (highScoreObj instanceof JSONObject scoreJson && scoreJson.get("timeRemaining") != null) {
                    long timeRemainingSeconds = (long) scoreJson.get("timeRemaining");
                    String difficultyName = scoreJson.get("difficulty").toString();
					long totalScore = (long) scoreJson.get("totalScore");

                    if (timeRemainingSeconds > 0) {
                        allScores.add(new ScoreEntry(
                                username,
								totalScore,
                                timeRemainingSeconds,
                                difficultyName
                        ));
                    }
                }
            }
        }

        List<ScoreEntry> topScores = allScores.stream()
                                              .sorted(Comparator
                                                      .comparing(ScoreEntry::scoreTimeSeconds)
                                                      .thenComparing((s1, s2) -> {
                                                          Difficulty d1 = Difficulty.valueOf(s1.difficulty());
                                                          Difficulty d2 = Difficulty.valueOf(s2.difficulty());
                                                          return Integer.compare(d2.ordinal(), d1.ordinal());
                                                      })
                                              )
                                              .limit(5)
                                              .collect(Collectors.toList());

        System.out.println("\n Top 5 Leaderboard");
        if (topScores.isEmpty()) {
            System.out.println("No scores yet.");
            return;
        }

        System.out.printf("%-6s %-10s %-10s %-12s %s%n", "RANK", "SCORE", "TIME", "DIFF", "USERNAME");
        System.out.println("==========================================================");

        for (int i = 0; i < topScores.size(); i++) {
            ScoreEntry entry = topScores.get(i);
            System.out.printf(
                    "%-6d %-10s %-10s %-12s %s%n",
                    i + 1,
					entry.totalScore(),
                    entry.getFormattedTime(),
                    entry.difficulty(),
                    entry.username()
            );
        }
        System.out.println("==========================================================");
    }

    public record ScoreEntry(String username, long totalScore, long scoreTimeSeconds, String difficulty) {
        public String getFormattedTime() {
            Duration d = Duration.ofSeconds(scoreTimeSeconds);
            return String.format("%02d:%02d", d.toMinutesPart(), d.toSecondsPart());
        }
    }
}
