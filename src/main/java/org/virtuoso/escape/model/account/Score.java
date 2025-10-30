package org.virtuoso.escape.model.account;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;

/**
 * Holds information about the users score.
 *
 * @param timeRemaining The time remaining when the user finished the game.
 * @param difficulty The difficulty the user was playing on when they finished the game.
 * @param totalScore The overall high score of a user.
 *
 * @author gabri
 * @author Andrew
 */
public record Score(Duration timeRemaining, Difficulty difficulty, Long totalScore) {

    /**
     * Construct a score object an calculate score.
     *
     * @param timeRemaining The time remaining when the user finished the game.
     * @param difficulty The difficulty the user was playing on when they finished the game.
     */
    public Score(Duration timeRemaining, Difficulty difficulty) {
        this(timeRemaining, difficulty, calculateScore(timeRemaining, difficulty));
    }

    /** Calculate a score from timeRemaining, difficulty, and GameState data. */
    public static Long calculateScore(Duration timeRemaining, Difficulty difficulty) {
        return timeRemaining != null
                ? (Long) (timeRemaining.toSeconds()
                        - GameState.instance().penalty()
                        - (GameState.instance().hintsUsed() != null
                                ? GameState.instance().hintsUsed().values().stream()
                                        .collect(Collectors.summingInt(Integer::intValue))
                                : 0))
                : null;
    }

    /**
     * Write the score object to a JSONObject.
     *
     * @return A JSONObject with the score information.
     */
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("timeRemaining", timeRemaining == null ? null : timeRemaining.getSeconds());
        obj.put("difficulty", difficulty.toString());
        obj.put("totalScore", totalScore);
        return obj;
    }

    /**
     * Get the time remaining when the user finished the game.
     *
     * @return The time remaining when the user finished the game.
     */
    public Duration timeRemaining() {
        return timeRemaining;
    }

    /**
     * Get the difficulty set when the user finished the game.
     *
     * @return The difficulty set when the user finished the game.
     */
    public Difficulty difficulty() {
        return difficulty;
    }

    /**
     * Get the total score from the user's game.
     *
     * @return The total score from the user's game.
     */
    public Long totalScore() {
        return totalScore;
    }
}
