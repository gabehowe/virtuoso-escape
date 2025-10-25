package org.virtuoso.escape.model.account;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;

import java.time.Duration;

/**
 * Holds information about the users score.
 * 
 * @author gabri
 * @author Andrew
 */
public class Score {
	private Duration timeRemaining;
	private Difficulty difficulty;
	private long totalScore;

	/**
	 * Construct a score object an calculate score.
	 * 
	 * @param timeRemaining The time remaining when the user finished the game.
	 * @param difficulty The difficulty the user was playing on when they finished the game.
	 */
	public Score(Duration timeRemaining, Difficulty difficulty){
		this.timeRemaining = timeRemaining;
		this.difficulty = difficulty;
		this.totalScore = timeRemaining != null ? (long) timeRemaining.toSeconds() : -1;
	}

	/**
	 * Construct a score object with a set score.
	 * 
	 * @param timeRemaining The time remaining when the user finished the game.
	 * @param difficulty The difficulty the user was playing on when they finished the game.
	 * @param totalScore The overall high score of a user.
	 */
	public Score(Duration timeRemaining, Difficulty difficulty, long totalScore){
		this.timeRemaining = timeRemaining;
		this.difficulty = difficulty;
		this.totalScore = totalScore;
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
	 * @return The time remaining when the user finished the game.
	 */
	public Duration timeRemaining() {
		return timeRemaining;
	}

	/**
	 * Get the difficulty set when the user finished the game.
	 * @return The difficulty set when the user finished the game.
	 */
	public Difficulty difficulty() {
		return difficulty;
	}

	/**
	 * Get the total score from the user's game.
	 * @return The total score from the user's game.
	 */
	public long totalScore() {
		return totalScore;
	}

	/**
	 * Print score information.
	 */
    public String toString() {
        return "Score: {totalScore=" + totalScore + " timeRemaining='" + timeRemaining.toSeconds() + "s', difficulty='" + difficulty + "'}";
    }
}
