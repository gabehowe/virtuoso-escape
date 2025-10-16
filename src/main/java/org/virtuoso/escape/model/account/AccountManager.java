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
	public JSONObject accounts;
	public JSONObject gameStates;

	private AccountManager() {
		this.accounts = DataLoader.loadAccounts();
		this.gameStates = DataLoader.loadGameStates();
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
			System.out.println(account + "highScore='" + account.highScore() + "'");
			System.out.println("Signed in!");
			return Optional.of(account);
		}
		System.out.println("Not logged in");
		return Optional.empty();
	}

	public Optional<Account> newAccount(String username, String password) {
		GameState gameState = GameState.instance();
		Account account = login(username, password).orElse(new Account(username, password));
		System.out.println(account + "\nCreated new account!");
		gameState.begin(account);
		return Optional.of(account);
	}

	public void logout() {
		DataWriter.writeAccount();
	}

	private Account accountExists(String username, String password) {
		String hashedPassword = Account.hashPassword(password);
		//System.out.println(hashedPassword);
		for (Object id : this.accounts.keySet()) {
			Object value = this.accounts.get(id);
			if (value instanceof JSONObject acct) {
				Object highScore = acct.get("highScore");
				String uName = acct.get("username").toString();
				String pWord = acct.get("hashedPassword").toString();
				if (highScore instanceof JSONObject score) {
					long timeRemaining = (long) score.get("timeRemaining");
					Difficulty difficulty = Difficulty.valueOf(score.get("difficulty").toString());
					if (uName.equals(username) && pWord.equals(hashedPassword)) {
						return new Account(username, password, UUID.fromString(id.toString()),
								new Score(Duration.ofSeconds(timeRemaining), difficulty));
					}
				}
			}
		}
		return null;
	}
}