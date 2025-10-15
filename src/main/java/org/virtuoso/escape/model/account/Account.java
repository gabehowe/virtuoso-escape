package org.virtuoso.escape.model.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * @author Treasure
 * @author gabri
 */
public class Account {
	private String hashedPassword;
	private String username;
	private UUID id;
	private Score highScore;

	public Account(String username, String password) {
		this.username = username;
		this.hashedPassword = hashPassword(password);
		this.id = UUID.randomUUID();
	}

	public Account(String username, String password, UUID id, Score highScore) {
		this(username, password);
		this.id = id;
		this.highScore = highScore;
	}

	public void setHighScore(Score score) {
		this.highScore = score;
	}

	public String username() {
		return this.username;
	}

	public UUID id() {
		return this.id;
	}

	public Score highScore() {
		return this.highScore;
	}

	public static String hashPassword(String password) {
		try {
			SecureRandom random = new SecureRandom();
			//byte[] salt = new byte[16];
			//random.nextBytes(salt);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			//md.update(salt);
			byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
			String hex = "";
			for (byte b : hash) {
				hex += String.format("%02x", b);
			}
			return hex;
		} catch (NoSuchAlgorithmException e) {
			// TODO(gabri) come up with a more elegant way to proceed.
			throw new RuntimeException(e);
		}
	}

	public String hashedPassword() {
		return this.hashedPassword;
	}

	@Override
	public String toString() {
		return "Account{" + "username='" + username + '\'' +
				", hashedPassword='" + hashedPassword + '\'' +
				'}';
	}
}
