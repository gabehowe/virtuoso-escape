package org.virtuoso.escape.model.account;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;

import java.time.Duration;

/**
 * @param timeRemaining
 * @param difficulty
 * @author gabri
 */
public record Score(Duration timeRemaining, Difficulty difficulty) {
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("timeRemaining", timeRemaining.toSeconds());
		obj.put("difficulty", difficulty.toString());
		return obj;
	}

	public String toString() {
		return "Score: {timeRemaining='" + timeRemaining.toSeconds() + "s', difficulty='" + difficulty + "'}";
	}
}
