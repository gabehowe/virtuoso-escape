package org.virtuoso.escape.model.account;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.*;

/**
 * @author Treasure
 * @author gabri
 * @author Andrew
 */
public class AccountManager {
	private static AccountManager accountManager;
	private JSONObject accounts;
	private JSONObject gameStates;

	private AccountManager() {
		this.accounts = loadAccounts();
		this.gameStates = loadGameStates();
	}

	public static AccountManager instance() {
		if (accountManager == null) {
			accountManager = new AccountManager();
		}
		return accountManager;
	}

	public Optional<Account> login(String username, String password) {
		GameState gameState = GameState.instance();
		Account account = accountExists(username, password);
		if (account != null) {
			gameState.begin(account);
			System.out.println("Signed in!\n" + account);
			return Optional.of(account);
		}
		System.out.println("Account does not exist.");
		return Optional.empty();
	}

	public Optional<Account> newAccount(String username, String password) {
		if (username.length() <= 32 && password.length() <= 32) {
			GameState gameState = GameState.instance();
			Optional<Account> account = login(username, password);
			if (account.isPresent()) return account;
			Account newAccount = new Account(username, password);
			System.out.println("Created new account!\n" + account);
			gameState.begin(newAccount);
			return Optional.of(newAccount);
		}
		return Optional.empty();
	}

	public void logout() {
		DataWriter.writeAccount();
	}

	public JSONObject getAccounts() {
		return this.accounts;
	}

	public JSONObject getGameStates() {
		return this.gameStates;
	}

	private JSONObject loadAccounts() {
		return DataLoader.loadAccounts();
	}

	private JSONObject loadGameStates() {
		return DataLoader.loadGameStates();
	}

	private Account accountExists(String username, String password) {
		String hashedPassword = Account.hashPassword(password);
		//System.out.println(hashedPassword);
		for (Object id : this.accounts.keySet()) {
			Object value = this.accounts.get(id);
			if (value instanceof JSONObject acct) {
				String uName = acct.get("username").toString();
				String pWord = acct.get("hashedPassword").toString();
				Object highScore = acct.get("highScore");
				if (uName.equals(username) && pWord.equals(hashedPassword)) {
					if (highScore instanceof JSONObject score) {
						long timeRemaining = (long) score.get("timeRemaining");
						Difficulty difficulty = Difficulty.valueOf(score.get("difficulty").toString());
						return new Account(username, password, UUID.fromString(id.toString()),
								new Score(Duration.ofSeconds(timeRemaining), difficulty));
					}
				}
			}
		}
		return null;
	}
}
