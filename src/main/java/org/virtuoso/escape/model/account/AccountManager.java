package org.virtuoso.escape.model.account;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.DataLoader;
import org.virtuoso.escape.model.data.DataWriter;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages user accounts.
 *
 * @author Treasure
 */
public class AccountManager {
	private static AccountManager accountManager;
	private JSONObject accounts;
	private JSONObject gameStates;

	/**
	 * Loads all user accounts and gamestates.
	 */
	private AccountManager() {
		this.accounts = loadAccounts();
		this.gameStates = loadGameStates();
	}

	/**
	 * The static {@code AccountManager}.
	 *
	 * @return the singleton of {@code AccountManager}
	 */
	public static AccountManager instance() {
		if (accountManager == null) {
			accountManager = new AccountManager();
		}
		return accountManager;
	}

	/**
	 * Attempts to log in with the indicated username and password.
	 *
	 * @param username The username to attempt.
	 * @param password The password to attempt.
	 * @return the {@code Optional<Account>} if username-password combination was valid, otherwise {@code Optional.empty}.
	 */
	public Optional<Account> login(String username, String password) {
		GameState gameState = GameState.instance();
		Account account = accountExists(username, password);
		if (account != null) {
			gameState.begin(account);
			return Optional.of(account);
		}
		return Optional.empty();
	}

	/**
	 * Attempts to create an {@link Account} with the indicated username and password.
	 *
	 * @param username The username to attempt.
	 * @param password The password to attempt.
	 * @return the {@code Optional<Account>} if no user exists with this exact username-password combination, otherwise {@code Optional.empty}.
	 */
	public Optional<Account> newAccount(String username, String password) {
		boolean usernameExists = false;

		Optional<Account> account = login(username, password);
		if (account.isPresent()) return account;

		for (Object id : this.accounts.keySet()) {
			JSONObject value = (JSONObject) this.accounts.get(id);
			if (value.get("username").equals(username)) usernameExists = true;
		}
		if (!usernameExists && username.length() <= 32 && password.length() <= 32) {
			GameState gameState = GameState.instance();

			Account newAccount = new Account(username, password);
			gameState.begin(newAccount);

			return Optional.of(newAccount);
		}
		return Optional.empty();
	}

	/**
	 * Logs the current user account out and writes its data.
	 */
	public void logout() {
		DataWriter.writeAccount();
	}

	/**
	 * Displays which information was incorrect when attempt to log in/create an account fails.
	 *
	 * @param username the username to be checked.
	 * @param password the password to be checked.
	 * @param signal the signal, represented as a character, that tells whether it was an attempt to log in or create an account.
	 * @return the respective string output based on which information was incorrect.
	 */
	public String getInvalidLoginInfo(String username, String password, char signal) {
		int usernameCount = 0;
		int passwordCount = 0;
		String hashedPassword = Account.hashPassword(password);
		for (Object id : this.accounts.keySet()) {
			JSONObject value = (JSONObject) this.accounts.get(id);
			String uName = value.get("username").toString();
			String pWord = value.get("hashedPassword").toString();
			if (uName.equals(username)) usernameCount++;
			else if (pWord.equals(hashedPassword)) passwordCount++;
		}
		if (Character.toLowerCase(signal) == 'l') {
			if (usernameCount == 0 && passwordCount == 0) return "Both username and password input is invalid.";
			else if (usernameCount > 0) return "Password input is invalid.";
			else return "Username input is invalid.";
		} else return "Username already exists.";
	}

	/**
	 * Gets all the user accounts.
	 *
	 * @return a JSONObject of {@code accounts}.
	 */
	public JSONObject getAccounts() {
		return this.accounts;
	}

	/**
	 * Gets all the gamestates of each user account.
	 *
	 * @return a JSONObject of {@code gameStates}.
	 */
	public JSONObject getGameStates() {
		return this.gameStates;
	}

	/**
	 * Calls {@link DataLoader}'s loadAccounts method.
	 *
	 * @return a JSONObject of all the user accounts.
	 */
	private JSONObject loadAccounts() {
		return DataLoader.loadAccounts();
	}

	/**
	 * Calls {@link DataLoader}'s loadGameStates method.
	 *
	 * @return a JSONObject of the gamestates.
	 */
	private JSONObject loadGameStates() {
		return DataLoader.loadGameStates();
	}

	/**
	 * Checks to see if an {@code Account} exists given a username and password.
	 *
	 * @param username the username to be checked.
	 * @param password the password to be checked.
	 * @return the {@code Account} with the indicated username and password if .
	 */
	private Account accountExists(String username, String password) {
		String hashedPassword = Account.hashPassword(password);
		for (Object id : this.accounts.keySet()) {
			Object value = this.accounts.get(id);
			if (value instanceof JSONObject acct) {
				String uName = acct.get("username").toString();
				String pWord = acct.get("hashedPassword").toString();
				Object highScore = acct.get("highScore");
				Boolean ttsStatus = (Boolean) acct.get("ttsOn");
				if (uName.equals(username) && (pWord.equals(hashedPassword))) {
					if (highScore instanceof JSONObject score) {
						Duration timeRemaining = score.get("timeRemaining") == null ? null : Duration.ofSeconds((Long) score.get("timeRemaining"));
						Difficulty difficulty = Difficulty.valueOf(score.get("difficulty").toString());
						return new Account(username, password, UUID.fromString(id.toString()), new Score(timeRemaining, difficulty), ttsStatus);
					}
				}
			}
		}
		return null;
	}
}
