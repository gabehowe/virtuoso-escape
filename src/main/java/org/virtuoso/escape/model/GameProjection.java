package org.virtuoso.escape.model;

import org.virtuoso.escape.model.account.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * @author Treasure
 */
public class GameProjection {
	private GameState gameState;
	private AccountManager accountManager;

	public GameProjection() {
		this.accountManager = AccountManager.instance();
		this.gameState = GameState.instance();
	}

	public boolean login(String username, String password) {
		Optional<Account> currentAccount = this.accountManager.login(username, password);
		return currentAccount.isPresent();
	}

	public boolean createAccount(String username, String password) {
		Optional<Account> currentAccount = this.accountManager.newAccount(username, password);
		return currentAccount.isPresent();
	}

	public void logout() {
		this.gameState.write();
		this.accountManager.logout();
	}

	public Room currentRoom() {
		return this.gameState.currentRoom();
	}

	public void pickRoom(Room room) {
		this.gameState.setCurrentRoom(room);
	}

	public List<Entity> roomEntities() {
		return currentRoom().entities();
	}

	public Optional<Entity> currentEntity() {
		return this.gameState.currentEntity();
	}

	public Floor currentFloor() {
		return this.gameState.currentFloor();
	}

	public Optional<String> currentMessage() {
		return this.gameState.currentMessage();
	}

	public List<Item> currentItems() {
		return this.gameState.currentItems();
	}

	public void addItem(Item item) {
		this.gameState.addItem(item);
	}

	public void setDifficulty(Difficulty difficulty) {
		this.gameState.setDifficulty(difficulty);
	}

	public void pickEntity(Entity entity) {
		this.gameState.pickEntity(entity);
	}

	public void leaveEntity() {
		this.gameState.leaveEntity();
	}

	public Duration time() {
		return this.gameState.time();
	}

	public void interact() {
		currentEntity().orElseThrow().interact();
	}

	public void attack() {
		currentEntity().orElseThrow().attack();
	}

	public void inspect() {
		currentEntity().orElseThrow().inspect();
	}

	public void input(String input) {
		currentEntity().orElseThrow().takeInput(input);
	}

	public boolean isEnded() {
		return this.gameState.isEnded();
	}
}
