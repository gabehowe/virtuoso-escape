package org.virtuoso.escape.model.account;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;

import java.time.Duration;

/**
 * The user's high score.
 * @param timeRemaining the time remaining in the game.
 * @param difficulty the difficulty the user played with.
 * @author gabri
 */
public record Score(Duration timeRemaining, Difficulty difficulty) {
	/**
	 * Converts the score to a JSONObject.
	 * @return the score in a JSONObject format.
	 */
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("timeRemaining", timeRemaining == null ? null : timeRemaining.getSeconds());
        obj.put("difficulty", difficulty.toString());
        return obj;
    }

	/**
	 * Prints out a string containing the user's time remaining and difficulty.
	 * @return the string representation of the user's high score.
	 */
	@Override
    public String toString() {
        return "Score: {timeRemaining='" + timeRemaining.toSeconds() + "s', difficulty='" + difficulty + "'}";
    }
}
