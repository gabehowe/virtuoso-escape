package org.virtuoso.escape.model.account;

import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameState;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

/**
 * A user account containing UUID, username, password, and high score.
 * @author Treasure
 * @author gabri
 */
public class Account {
	private String hashedPassword;
	private String username;
	private UUID id;
	private Score highScore;

	/**
	 * Creates an {@code Account} with the indicated username and password.
	 * @param username the username to be assigned.
	 * @param password the password to be assigned.
	 */
	public Account(String username, String password) {
		this.username = username;
		this.hashedPassword = hashPassword(password);
		this.highScore = new Score(null, Difficulty.TRIVIAL);
		this.id = UUID.randomUUID();
	}

	/**
	 * Loads an {@code Account} with the pre-existing UUID, username, password, and high score.
	 * @param username the username of the account.
	 * @param password the password of the account
	 * @param id the UUID of the account.
	 * @param highScore the high score of the account.
	 */
	public Account(String username, String password, UUID id, Score highScore) {
		this(username, password);
		this.id = id;
		this.highScore = highScore;
	}

	/**
	 * Sets the high score with the indicated {@link Score}.
	 * @param score the score to be assigned.
	 */
	public void setHighScore(Score score) {
		this.highScore = score;
	}

	/**
	 * Gets the username of the account.
	 * @return the username of the account.
	 */
	public String username() {
		return this.username;
	}

	/**
	 * Gets the UUID of the account.
	 * @return the UUID of the account.
	 */
	public UUID id() {
		return this.id;
	}

	/**
	 * Gets the high score of the account.
	 * @return the {@code highScore} of the account.
	 */
	public Score highScore() {
		return this.highScore;
	}

	/**
	 * Gets the hashed password of the account.
	 * @return the {@code hashedPassword} of the account
	 */
	public String hashedPassword() {
		return this.hashedPassword;
	}

	/**
	 * Hashes the indicated password.
	 * @param password the password to be hashed.
	 * @return a string representation of the {@code hashedPassword}.
	 */
	public static String hashPassword(String password) {
		try {
			SecureRandom random = new SecureRandom();
			//byte[] salt = new byte[16];
			//random.nextBytes(salt);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			//md.update(salt);
			byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
			String hex = "";
			for (byte b : hash) hex += String.format("%02x", b);
			return hex;
		} catch (NoSuchAlgorithmException e) {
			// TODO(gabri) come up with a more elegant way to proceed.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Prints a string containing the UUID, the username, and the high score of the account.
	 * @return a string representation of the account information.
	 */
	@Override
	public String toString() {
		return "Account: {" + "id='" + this.id + "', " + "username='" + this.username + "', " + this.highScore.toString();
	}
}
