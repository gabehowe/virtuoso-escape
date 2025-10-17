package org.virtuoso.escape.model.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.*;
import org.virtuoso.escape.model.account.Score;

/**
 * @author Andrew
 */
public class DataWriter {
	private static final String GAME_STATES_PATH = "json/gamestates.json";
	private static final String ACCOUNTS_PATH = "json/accounts.json";

	@SuppressWarnings("unchecked")
	public static void writeGameState() {
		GameState currentGameState = GameState.instance();
		JSONObject currentGameStateMap = loadGameStateInfo(currentGameState);
		JSONObject allGameStatesMap = AccountManager.instance().getGameStates();
		allGameStatesMap.put(currentGameState.account().id().toString(), currentGameStateMap);
		writeToFile(GAME_STATES_PATH, allGameStatesMap);
	}

	@SuppressWarnings("unchecked")
	public static void writeAccount() {
		Account currentAccount = GameState.instance().account();
		JSONObject currentAccountMap = loadAccountInfo(currentAccount);
		JSONObject allAccountsMap = AccountManager.instance().getAccounts();
		allAccountsMap.put(currentAccount.id().toString(), currentAccountMap);
		writeToFile(ACCOUNTS_PATH, allAccountsMap);
	}

	private static void writeToFile(String filepath, JSONObject json) {
		try (FileWriter file = new FileWriter(filepath)) {

			file.write(json.toJSONString());
			file.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static JSONObject loadGameStateInfo(GameState gameState) {
		JSONObject gameStateMap = new JSONObject();
		gameStateMap.put("currentFloor", gameState.currentFloor().id());
		gameStateMap.put("currentRoom", gameState.currentRoom().id());
		gameStateMap.put("currentEntity", gameState.currentEntity().isPresent() ? gameState.currentEntity().get().id() : null);
		gameStateMap.put("currentItems", itemIds(gameState.currentItems()));
		gameStateMap.put("time", gameState.time());
		gameStateMap.put("difficulty", gameState.difficulty().toString());
		return gameStateMap;
	}

	@SuppressWarnings("unchecked")
	private static JSONArray itemIds(List<Item> currentItems) {
		JSONArray itemJSON = new JSONArray();
		currentItems.stream().map(Item::id).forEach(itemJSON::add);
		return itemJSON;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject loadAccountInfo(Account account) {
		JSONObject accountMap = new JSONObject();
		JSONObject highScore = account.highScore().toJSON();
		accountMap.put("username", account.username());
		accountMap.put("hashedPassword", account.hashedPassword());
		accountMap.put("highScore", highScore);
		return accountMap;
	}
}
