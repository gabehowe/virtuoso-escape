package org.virtuoso.escape.model.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Entity;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Item;
import org.virtuoso.escape.model.Room;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew
 */
public class DataWriter {
    private static final String GAME_STATES_PATH = "json/gamestates.json";
    private static final String ACCOUNTS_PATH = "json/accounts.json";

    /**
     * Write {@link GameState#instance()} to a {@link DataWriter#GAME_STATES_PATH}.
     */
    @SuppressWarnings("unchecked")
    public static void writeGameState() {
        GameState currentGameState = GameState.instance();
        JSONObject currentGameStateMap = loadGameStateInfo(currentGameState);
        JSONObject allGameStatesMap = AccountManager.instance().gameStates();
        allGameStatesMap.put(currentGameState.account().id().toString(), currentGameStateMap);
        writeToFile(GAME_STATES_PATH, allGameStatesMap);
    }

    /**
     * Write the current account ({@link GameState#instance()#account()}) to {@link DataWriter#ACCOUNTS_PATH}.
     */
    @SuppressWarnings("unchecked")
    public static void writeAccount() {
        Account currentAccount = GameState.instance().account();
        JSONObject currentAccountMap = loadAccountInfo(currentAccount);
        JSONObject allAccountsMap = AccountManager.instance().accounts();
        allAccountsMap.put(currentAccount.id().toString(), currentAccountMap);
        writeToFile(ACCOUNTS_PATH, allAccountsMap);
    }

    /**
     * Write a {@link JSONObject} to a file.
     *
     * @param filepath The file to write to.
     * @param json     The object to write.
     */
    private static void writeToFile(String filepath, JSONObject json) {
        try (FileWriter file = new FileWriter(filepath)) {

            file.write(json.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a {@link JSONObject} for writing from a {@link GameState}.
     *
     * @param gameState The {@link GameState} to parse.
     * @return A {@link JSONObject} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject loadGameStateInfo(GameState gameState) {
        JSONObject gameStateMap = new JSONObject();
        gameStateMap.put("currentFloor", gameState.currentFloor().id());
        gameStateMap.put("currentRoom", gameState.currentRoom().id());
        gameStateMap.put("currentEntity", gameState.currentEntity().isPresent() ? gameState.currentEntity().get().id() : null);
        gameStateMap.put("currentItems", itemIds(gameState.currentItems()));
        gameStateMap.put("currentEntityStates", entityStates(gameState.currentFloor().rooms()));
        gameStateMap.put("time", gameState.time().getSeconds());
        gameStateMap.put("difficulty", gameState.difficulty().toString());
		gameStateMap.put("completedPuzzles", completedPuzzleIds(gameState.completedPuzzles().stream().toList()));
		gameStateMap.put("hintsUsed", hintsUsedJSON(gameState.usedHints()));
        return gameStateMap;
    }

    /**
     * Create a {@link JSONArray} from a {@link List<Item>}.
     *
     * @param currentItems The item list to parse.
     * @return A {@link JSONArray} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONArray itemIds(List<Item> currentItems) {
        JSONArray itemJSON = new JSONArray();
        currentItems.stream().map(Item::id).forEach(itemJSON::add);
        return itemJSON;
    }

	/**
     * Create a {@link JSONArray} from a {@link List<String>}.
     *
     * @param completedLevels Strings of the puzzles that have been completed.
     * @return A {@link JSONArray} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONArray completedPuzzleIds(List<String> completedPuzzles) {
        JSONArray levelJSON = new JSONArray();
        completedPuzzles.stream().forEach(levelJSON::add);
        return levelJSON;
    }

	/**
     * Create a {@link JSONArray} from a {@link Map<String, Integer>}.
     *
     * @param completedLevels Strings of the levels that have been completed.
     * @return A {@link JSONArray} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject hintsUsedJSON(Map<String, Integer> hintsUsed) {
        return new JSONObject(hintsUsed);
    }

    /**
     * Create a {@link JSONArray} from a {@link List<Room>}.
     *
     * @param currentRooms The room list to parse.
     * @return A {@link JSONArray} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject entityStates(List<Room> currentRooms) {
        JSONObject entityJSON = new JSONObject();
        currentRooms.stream().map(Room::entities)
                    .flatMap(list -> list.stream()).map(Entity::write)
                    .forEach(pair -> entityJSON.put(pair[0], pair[1]));
        return entityJSON;
    }

    /**
     * Create a {@link JSONObject} for writing from an {@link Account}.
     *
     * @param account The {@link Account} to parse.
     * @return A {@link JSONObject} ready for writing.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject loadAccountInfo(Account account) {
        JSONObject accountMap = new JSONObject();
        accountMap.put("username", account.username());
        accountMap.put("hashedPassword", account.hashedPassword());
        accountMap.put("highScore", account.highScore().toJSON());
		accountMap.put("ttsOn", account.ttsOn());
        return accountMap;
    }
}
