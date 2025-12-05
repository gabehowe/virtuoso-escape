package org.virtuoso.escape.model.account;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;

/** Leaderboard Displays high scores per user accounts */
public class Leaderboard {

    /**
     * Update the user's high score.
     *
     * @param username The user's username.
     */
    public static void recordSession(String username) {
        GameState state = GameState.instance();
        Account account = state.account();

        if (account != null) {
            Score newScore = new Score(state.time(), state.difficulty());
            Score currentHighScore = account.highScore();

            if (Objects.nonNull(newScore.totalScore())
                    && Objects.nonNull(currentHighScore.totalScore())
                    && newScore.totalScore() > currentHighScore.totalScore()) {
                account.setHighScore(newScore);
                System.out.println("Score updated");
            }
        }
    }

    /** Print the leaderboard of scores and difficulties. */
    public static void showLeaderboard() {
        JSONObject accountsJson = AccountManager.instance().accounts();
        List<ScoreEntry> allScores = new ArrayList<>();

        for (Object id : accountsJson.keySet()) {
            Object value = accountsJson.get(id);
            if (value instanceof JSONObject acct) {
                String username = acct.get("username").toString();
                Object highScoreObj = acct.get("highScore");
                if (highScoreObj instanceof JSONObject scoreJson
                        && scoreJson.get("timeRemaining") != null
                        && scoreJson.get("totalScore") != null) {
                    Long timeRemainingSeconds = (Long) scoreJson.get("timeRemaining");
                    String difficultyName = scoreJson.get("difficulty").toString();
                    Long totalScore = (Long) scoreJson.get("totalScore");

                    if (timeRemainingSeconds > 0) {
                        allScores.add(new ScoreEntry(username, totalScore, timeRemainingSeconds, difficultyName));
                    }
                }
            }
        }
        allScores.add(new ScoreEntry(
                GameState.instance().account().username(),
                Score.calculateScore(
                        GameState.instance().time(), GameState.instance().difficulty()),
                (Long) GameState.instance().time().toSeconds(),
                GameState.instance().difficulty().toString()));

        List<ScoreEntry> topScores = allScores.stream()
                .sorted(Comparator.comparing(ScoreEntry::totalScore)
                        .thenComparing((s1, s2) -> {
                            Difficulty d1 = Difficulty.valueOf(s1.difficulty());
                            Difficulty d2 = Difficulty.valueOf(s2.difficulty());
                            return Integer.compare(d2.ordinal(), d1.ordinal());
                        })
                        .reversed())
                .limit(5)
                .collect(Collectors.toList());

        System.out.println("\nTop 5 Leaderboard");
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
                    i + 1, entry.totalScore(), entry.getFormattedTime(), entry.difficulty(), entry.username());
        }
        System.out.println("==========================================================");
    }

    /**
     * Returns a list format version of the leaderboard
     *
     * @return A list of every leaderboard element, row by row, left to right.
     */
    public static List<String> getLeaderboard() {
        JSONObject accountsJson = AccountManager.instance().accounts();
        List<ScoreEntry> allScores = new ArrayList<>();

        for (Object id : accountsJson.keySet()) {
            Object value = accountsJson.get(id);
            if (value instanceof JSONObject acct) {
                String username = acct.get("username").toString();
                Object highScoreObj = acct.get("highScore");
                if (highScoreObj instanceof JSONObject scoreJson
                        && scoreJson.get("timeRemaining") != null
                        && scoreJson.get("totalScore") != null) {
                    Long timeRemainingSeconds = (Long) scoreJson.get("timeRemaining");
                    String difficultyName = scoreJson.get("difficulty").toString();
                    Long totalScore = (Long) scoreJson.get("totalScore");

                    if (timeRemainingSeconds > 0) {
                        allScores.add(new ScoreEntry(username, totalScore, timeRemainingSeconds, difficultyName));
                    }
                }
            }
        }
        allScores.add(new ScoreEntry(
                GameState.instance().account().username(),
                Score.calculateScore(
                        GameState.instance().time(), GameState.instance().difficulty()),
                (Long) GameState.instance().time().toSeconds(),
                GameState.instance().difficulty().toString()));

        List<ScoreEntry> top_scores = allScores.stream()
                .sorted(Comparator.comparing(ScoreEntry::totalScore, Comparator.reverseOrder())
                .thenComparing(s -> Difficulty.valueOf(s.difficulty()), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());

        ArrayList<String> output_array = new ArrayList<String>();
        int place = 1;
        for (ScoreEntry score : top_scores) {
            List<String> to_add = Arrays.asList(
                    String.valueOf(place),
                    String.valueOf(score.totalScore()),
                    score.username(),
                    String.valueOf(score.getFormattedTime()));
            output_array.addAll(to_add);
            place++;
        }
        return output_array;
    }

    public record ScoreEntry(String username, Long totalScore, Long scoreTimeSeconds, String difficulty) {
        public String getFormattedTime() {
            Duration d = Duration.ofSeconds(scoreTimeSeconds);
            return String.format("%02d:%02d", d.toMinutesPart(), d.toSecondsPart());
        }
    }
}
