package org.virtuoso.escape.model.account;

import org.virtuoso.escape.model.Difficulty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * A user account containing UUID, username, password, and high score.
 *
 * @author Treasure
 * @author gabri
 */
public class Account {
    private String hashedPassword;
    private String username;
    private UUID id;
    private Score highScore;
	private boolean ttsOn;

    /**
     * Create an {@code Account} with the indicated username and password.
     *
     * @param username the username to be assigned.
     * @param password the password to be assigned.
     */
    public Account(String username, String password) {
        this.username = username;
        this.hashedPassword = hashPassword(password);
        this.highScore = new Score(null, Difficulty.TRIVIAL);
        this.id = UUID.randomUUID();
		this.ttsOn = true;
    }

    /**
     * Load an {@code Account} with the pre-existing UUID, username, password, and high score.
     *
     * @param username  the username of the account.
     * @param password  the password of the account
     * @param id        the UUID of the account.
     * @param highScore the high score of the account.
	 * @param ttsOn     whether of not text to speech is on for this account.
     */
    public Account(String username, String password, UUID id, Score highScore, boolean ttsOn) {
        this(username, password);
        this.id = id;
        this.highScore = highScore;
		this.ttsOn = ttsOn;
    }

    /**
     * Hashes the indicated password.
     *
     * @param password the password to be hashed.
     * @return a string representation of the {@code hashedPassword}.
     */
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
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
     * Set the high score with the indicated {@link Score}.
     *
     * @param score the score to be assigned.
     */
    public void setHighScore(Score score) {
        this.highScore = score;
    }

	/**
	 * Set text to speech to either on or off
	 * @param ttsOn Whether or not text to speech is enabled on this account.
	 */
	public void SetTtsOn(boolean ttsOn){
		this.ttsOn = ttsOn;
	}

    /**
     * Get the username of the account.
     *
     * @return the username of the account.
     */
    public String username() {
        return this.username;
    }

    /**
     * Get the UUID of the account.
     *
     * @return the UUID of the account.
     */
    public UUID id() {
        return this.id;
    }

    /**
     * Get the high score of the account.
     *
     * @return the {@code highScore} of the account.
     */
    public Score highScore() {
        return this.highScore;
    }

    /**
     * Get the hashed password of the account.
     *
     * @return the {@code hashedPassword} of the account
     */
    public String hashedPassword() {
        return this.hashedPassword;
    }

	/**
	 * Get whether or not text to speech is enabled on this account.
	 * @return Whether or not text to speech is enabled on this account.
	 */
	public boolean ttsOn(){
		return this.ttsOn;
	}

    /**
     * Print a string containing the UUID, the username, and the high score of the account.
     *
     * @return a string representation of the account information.
     */
    @Override
    public String toString() {
        return "Account: {" + "id='" + this.id + "', " + "username='" + this.username + "', " + this.highScore.toString();
    }
}
