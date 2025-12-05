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
public record GameProjection() {

    /**
     * Initialize and attempt login.
     *
     * @param username The username to attempt.
     * @param password The password to attempt.
     * @return {@code true} if the username-password combination was valid, otherwise {@code false}.
     */
    public boolean login(String username, String password) {
        Optional<Account> currentAccount = AccountManager.instance().login(username, password);
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
        Optional<Account> currentAccount = AccountManager.instance().newAccount(username, password);
        return currentAccount.isPresent();
    }

    /** Log the current user out and write data. */
    public void logout() {
        SpeechPlayer.instance().stopSoundbite();
        GameState.instance().write();
        AccountManager.instance().logout();
    }

    /**
     * The room the user is in.
     *
     * @return The room the user is in.
     */
    public Room currentRoom() {
        return GameState.instance().currentRoom();
    }

    /**
     * Change the current room.
     *
     * @param room The room to change to.
     */
    public void pickRoom(Room room) {
        GameState.instance().setCurrentRoom(room);
    }

    /**
     * The currently focused entity.
     *
     * @return The {@link Optional<Entity>} if the current entity is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<Entity> currentEntity() {
        return GameState.instance().currentEntity();
    }

    /**
     * The current floor.
     *
     * @return The current floor.
     */
    public Floor currentFloor() {
        return GameState.instance().currentFloor();
    }

    /**
     * Consume the current message.
     *
     * @return {@link Optional<String>} if the message is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<String> currentMessage() {
        return GameState.instance().currentMessage();
    }

    /**
     * The full inventory of the user.
     *
     * @return A list of items.
     */
    public List<Item> currentItems() {
        return GameState.instance().currentItems();
    }

    /**
     * Set the difficulty to {@code difficulty}
     *
     * @param difficulty The {@link Difficulty} to change to.
     */
    public void setDifficulty(Difficulty difficulty) {
        GameState.instance().setDifficulty(difficulty);
    }

    /**
     * Focus {@code entity}
     *
     * @param entity The {@link Entity} to focus.
     */
    public void pickEntity(Entity entity) {
        GameState.instance().pickEntity(entity);
    }

    /** Unfocus the current entity. */
    public void leaveEntity() {
        GameState.instance().leaveEntity();
    }

    /**
     * The time remaining on the countdown.
     *
     * @return The time remaining on the countdown.
     */
    public Duration time() {
        return GameState.instance().time();
    }

    /** Increments the {@code initialTime} by 1 minute if it is less than 2 hours. */
    public void incrementInitialTime() {
        GameState.instance().incrementInitialTime();
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
        return GameState.instance().isEnded();
    }
}
