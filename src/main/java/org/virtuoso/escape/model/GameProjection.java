package org.virtuoso.escape.model;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.speech.SpeechPlayer;

/**
 * The game facade.
 *
 * @author Treasure
 */
public record GameProjection(GameState gameState, AccountManager accountManager) {

    /** Create the default projection without state. */
    public GameProjection() {
        this(GameState.instance(), AccountManager.instance());
    }

    /**
     * Initialize and attempt login.
     *
     * @param username The username to attempt.
     * @param password The password to attempt.
     * @return {@code true} if the username-password combination was valid, otherwise {@code false}.
     */
    public boolean login(String username, String password) {
        Optional<Account> currentAccount = this.accountManager.login(username, password);
        return currentAccount.isPresent();
    }

    /**
     * Attempt to create an account.
     *
     * @param username The username to attempt.
     * @param password The password to attempt.
     * @return {@code true} if no user exists with this exact username-password combination, otherwise {@code false}.
     */
    public boolean createAccount(String username, String password) {
        Optional<Account> currentAccount = this.accountManager.newAccount(username, password);
        return currentAccount.isPresent();
    }

    /** Log the current user out and write data. */
    public void logout() {
        SpeechPlayer.instance().stopSoundbite();
        this.gameState.write();
        this.accountManager.logout();
    }

    /**
     * The room the user is in.
     *
     * @return The room the user is in.
     */
    public Room currentRoom() {
        return this.gameState.currentRoom();
    }

    /**
     * Change the current room.
     *
     * @param room The room to change to.
     */
    public void pickRoom(Room room) {
        this.gameState.setCurrentRoom(room);
    }

    /**
     * The currently focused entity.
     *
     * @return The {@link Optional<Entity>} if the current entity is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<Entity> currentEntity() {
        return this.gameState.currentEntity();
    }

    /**
     * The current floor.
     *
     * @return The current floor.
     */
    public Floor currentFloor() {
        return this.gameState.currentFloor();
    }

    /**
     * Consume the current message.
     *
     * @return {@link Optional<String>} if the message is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<String> currentMessage() {
        return this.gameState.currentMessage();
    }

    /**
     * The full inventory of the user.
     *
     * @return A list of items.
     */
    public List<Item> currentItems() {
        return this.gameState.currentItems();
    }

    /**
     * Set the difficulty to {@code difficulty}
     *
     * @param difficulty The {@link Difficulty} to change to.
     */
    public void setDifficulty(Difficulty difficulty) {
        this.gameState.setDifficulty(difficulty);
    }

    /**
     * Focus {@code entity}
     *
     * @param entity The {@link Entity} to focus.
     */
    public void pickEntity(Entity entity) {
        this.gameState.pickEntity(entity);
    }

    /** Unfocus the current entity. */
    public void leaveEntity() {
        this.gameState.leaveEntity();
    }

    /**
     * The time remaining on the countdown.
     *
     * @return The time remaining on the countdown.
     */
    public Duration time() {
        return this.gameState.time();
    }

    /** Increments the {@code initialTime} by 1 minute if it is less than 2 hours. */
    public void incrementInitialTime() {
        this.gameState.incrementInitialTime();
    }

    /**
     * Interact with the currently focused entity.
     *
     * @throws java.util.NoSuchElementException if no entity is focused.
     */
    public void interact() {
        currentEntity().orElseThrow().state().interact();
    }

    /**
     * Attack the currently focused entity.
     *
     * @throws java.util.NoSuchElementException if no entity is focused.
     */
    public void attack() {
        currentEntity().orElseThrow().state().attack();
    }

    /**
     * Inspect the currently focused entity.
     *
     * @throws java.util.NoSuchElementException if no entity is focused.
     */
    public void inspect() {
        currentEntity().orElseThrow().state().inspect();
    }

    /**
     * Speak to the current entity.
     *
     * @param input The input to give to the entity.
     * @throws java.util.NoSuchElementException if no entity is focused.
     */
    public void input(String input) {
        currentEntity().orElseThrow().state().takeInput(input);
    }

    /**
     * The capabilities of the current entity -- whether it supports an action.
     *
     * @return The capabilities of the entity.
     */
    public EntityState.Capabilities capabilities() {
        return currentEntity().orElseThrow().state().capabilities();
    }

    /**
     * Whether the game is ended.
     *
     * @return {@code true} if the game has ended, otherwise {@code false}.
     */
    public boolean isEnded() {
        return this.gameState.isEnded();
    }
}
