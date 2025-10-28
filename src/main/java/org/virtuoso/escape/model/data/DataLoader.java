package org.virtuoso.escape.model.data;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.account.Score;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads data from JSON files.
 *
 * @author Bose
 * @author Treasure
 * @author Andrew
 */
public class DataLoader {
    public static String ACCOUNTS_PATH = "accounts.json";
    public static String LANGUAGE_PATH = "language.json";
    public static String GAMESTATES_PATH = "gamestates.json";
    /**
     * Load all accounts from accounts.json
     *
     * @return An id-account accounts map.
     */
    public static JSONObject loadAccounts() {
        JSONObject result = new JSONObject();
        JSONObject root = parseJsonFile(Path.of("json", DataLoader.ACCOUNTS_PATH));
        if (root == null) return result;
        for (Object key : root.keySet()) {
            String id = String.valueOf(key);
            Object value = root.get(id);
            if (value instanceof JSONObject acct) {
                String username = acct.get("username").toString();
                String hashed = acct.get("hashedPassword").toString();
                Object highScore = acct.get("highScore");
				Object ttsOn = acct.getOrDefault("ttsOn", true);
                if (highScore instanceof JSONObject)
                    result.put(id, new JSONObject(Map.of(
                            "username", username,
                            "hashedPassword", hashed,
                            "highScore", highScore,
							"ttsOn", ttsOn
                    )));
            }
        }
        return result;
    }

    /**
     * Loads language mapping from language.json
     *
     * @return A mapping of id: (id: string)
     */
    public static Map<String, Map<String, String>> loadGameLanguage() {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        JSONObject root = parseJsonFile(Path.of("json", LANGUAGE_PATH));
        if (root == null) return result;
        for (Object key : root.keySet()) {
            String id = String.valueOf(key);
            Object inner = root.get(id);
            if (inner instanceof JSONObject innerObj) {
                Map<String, String> map = new HashMap<String, String>();
                for (Object sKey : innerObj.keySet()) {
                    String sk = String.valueOf(sKey);
                    Object sval = innerObj.get(sk);
                    if (sval != null) map.put(sk, String.valueOf(sval));
                }
                result.put(id, map);
            }
        }
        return result;
    }

    /**
     * Loads all high scores from a file.
     *
     * @return A id-score mapping.
     */
    public static Map<String, Score> loadHighScores() {
        Map<String, Score> result = new HashMap<>();
        JSONObject root = parseJsonFile(Path.of("json", ACCOUNTS_PATH));
        if (root == null) return result;

        for (Object key : root.keySet()) {
            String id = String.valueOf(key);
            Object value = root.get(id);
            if (value instanceof JSONObject acct) {
                Object high = acct.get("highScore");
                if (high instanceof JSONObject score) {
                    long seconds = (long) score.get("timeRemaining");
                    Difficulty difficulty = Difficulty.valueOf(score.get("difficulty").toString());
                    Score highScore = new Score(Duration.ofSeconds(seconds), difficulty);
                    result.put(id, highScore);
                }
            }
        }
        return result;
    }

    /**
     * Load all gamestates from gamestates.json
     *
     * @return An id-gamestate mapping.
     */
    public static JSONObject loadGameStates() {
        JSONObject result = new JSONObject();
        JSONObject root = parseJsonFile(Path.of("json", GAMESTATES_PATH));
        if (root == null) return result;
        for (Object key : root.keySet()) {
            String id = String.valueOf(key);
            Object inner = root.get(id);
            if (inner instanceof JSONObject innerObj) {
                JSONObject map = new JSONObject();
                for (Object sKey : innerObj.keySet()) {
                    String sk = String.valueOf(sKey);
                    Object sval = innerObj.get(sk);
                    if (sval != null) {
                        if (sk.equals("currentItems")) {
                            sval = innerObj.get(sk);
                            map.put(sk, sval);
						} else if (sk.equals("completedPuzzles")) {
                            sval = innerObj.get(sk);
                            map.put(sk, sval);
                        } else if (sk.equals("time")) {
                            sval = innerObj.get(sk);
                            map.put(sk, sval);
                        } else if (sk.equals("currentEntityStates")) {
                            sval = innerObj.get(sk);
                            map.put(sk, sval);
						 } else if (sk.equals("hintsUsed")) {
                            sval = innerObj.get(sk);
                            map.put(sk, sval);
                        } else map.put(sk, String.valueOf(sval));
                    }
                }
                result.put(id, map);
            }
        }
        return result;
    }

    /**
     * Parses a JSON file into a {@link JSONObject}.
     *
     * @param file The file to parse
     * @return The loaded into a {@link JSONObject}.
     */
    private static JSONObject parseJsonFile(Path file) {
        if (!Files.exists(file)) {
            System.err.println("File not found");
            return null;
        }
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file.toFile())) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONObject) return (JSONObject) obj;
            System.err.println("Not viable");
        } catch (IOException | ParseException e) {
            System.err.println("Could not parse file");
        }
        return null;
    }
}
