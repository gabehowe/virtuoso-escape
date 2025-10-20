package org.virtuoso.escape.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.Score;
import org.virtuoso.escape.model.data.DataLoader;
import org.virtuoso.escape.model.data.DataWriter;

import java.time.Duration;
import java.util.*;

/**
 * The current state of the game.
 * Holds most mutable data.
 * @author Andrew
 */
public class GameState {
	private static GameState instance;
	private Floor currentFloor;
	private Room currentRoom;
	private Entity currentEntity;
	private Set<Item> currentItems;
	private Duration time;
	public static final long initialTime = 2700;
	private long startTime;
	private Account account;
	private boolean ended;

	private Difficulty difficulty;
	private String currentMessage;

	/**
	 * The global singleton.
	 * @return The global singleton.
	 */
	public static GameState instance() {
		if (instance == null)
			instance = new GameState();
		return instance;
	}

	/**
	 * Initialize the singleton with data.
	 * @param account The account to draw data with.
	 */
	public void begin(Account account) {
		JSONObject gameStateInfo = (JSONObject) DataLoader.loadGameStates().getOrDefault(account.id().toString(), new JSONObject());
		this.currentFloor = GameInfo.instance().building().stream().filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentFloor"))).findFirst().orElse(GameInfo.instance().building().getFirst());
		this.currentRoom = currentFloor.rooms().stream().filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentRoom"))).findFirst().orElse(currentFloor.rooms().getFirst());
		this.currentEntity = currentRoom.entities().stream().filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentEntity"))).findFirst().orElse(null);
		this.currentItems = new HashSet<>();
		JSONArray items = (JSONArray) gameStateInfo.getOrDefault("currentItems", new JSONArray());
		for (int i = 0; i < items.size(); i++) {
			currentItems.add(Item.valueOf((String) items.get(i)));
		}
		this.time = Duration.ofSeconds((Long) gameStateInfo.getOrDefault("time", initialTime));
		this.account = account;
		this.difficulty = Difficulty.valueOf((String) gameStateInfo.getOrDefault(difficulty, "SUBSTANTIAL"));
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Focus {@code entity}
	 * @param entity The {@link Entity} to focus.
	 */
	public void pickEntity(Entity entity) {
		this.currentEntity = entity;
	}

	/**
	 * Unfocus the current entity.
	 */
	public void leaveEntity() {
		this.currentEntity = null;
	}

	/**
	 * Whether the inventory contains {@code item}.
	 * @param item The item to check for.
	 * @return {@code true} if the inventory contains the item, otherwise {@code false}.
	 */
	public boolean hasItem(Item item) {
		return currentItems.contains(item);
	}

	/**
	 * Add an {@link Item} to the user's inventory.
	 * @param item The {@link Item} to add.
	 */
	public void addItem(Item item) {
		currentItems.add(item);
	}

	/**
	 * The current floor.
	 * @return The current floor.
	 */
	public Floor currentFloor() {
		return currentFloor;
	}

	/**
	 * Change to another floor.
	 * @param currentFloor The floor to change to.
	 */
	public void setCurrentFloor(Floor currentFloor) {
		this.currentFloor = currentFloor;
		this.currentEntity = null;
		this.setCurrentRoom(currentFloor.rooms().getFirst());
	}

	/**
	 * The currently focused entity.
	 * @return The {@link Optional<Entity>} if the current entity is non-null, otherwise {@link Optional#empty()}.
	 */
	public Optional<Entity> currentEntity() {
		return Optional.ofNullable(currentEntity);
	}

	/**
	 * Set the currently focused entity.
	 * @param currentEntity The entity to focus.
	 */
	public void setCurrentEntity(Entity currentEntity) {
		this.currentEntity = currentEntity;
	}

	/**
	 * The full inventory of the user.
	 * @return A list of items.
	 */
	public List<Item> currentItems() {
		return currentItems.stream().toList();
	}

	/**
	 * Remove all items.
	 */
	public void clearItems() {
		currentItems.clear();
	}

	/**
	 * The time remaining on the countdown.
	 * @return The time remaining on the countdown.
	 */
	public Duration time() {
		var delta = System.currentTimeMillis() - this.startTime;
		return time.minusMillis(delta);
	}

	/**
	 * Set the time remaining.
	 */
	public void setTime(Duration time) {
		this.time = time;
	}

	/**
	 * Add {@code time} to the countdown.
	 * @param time The amount of time to add.
	 */
	public void addTime(Duration time) {
		this.time = this.time.plus(time);
	}

	/**
	 * The account logged in with.
	 * @return The account logged in with.
	 */
	public Account account() {
		return account;
	}

	/**
	 * The current difficulty.
	 * @return The current difficulty.
	 */
	public Difficulty difficulty() {
		return difficulty;
	}

	/**
	 * Set the current difficulty.
	 * @param diff The difficulty to set to.
	 */
	public void setDifficulty(Difficulty diff) {
		this.difficulty = diff;
	}

	/**
	 * Recalculate the high score.
	 */
	public void updateHighScore() {
		if (isEnded()) {
			long currentTimeRemaining = initialTime - (this.time).getSeconds();
			long oldTimeRemaining = this.account.highScore().timeRemaining().getSeconds();
			if (oldTimeRemaining == 2700 || currentTimeRemaining > oldTimeRemaining) {
				this.account.setHighScore(new Score(Duration.ofSeconds(currentTimeRemaining), this.difficulty));
			}
		}
	}

	/**
	 * Consume the current message.
	 * @return {@link Optional<String>} if the message is non-null, otherwise {@link Optional#empty()}.
	 */
	public Optional<String> currentMessage() {
		String currentMessage = this.currentMessage;
		this.currentMessage = null;
		return Optional.ofNullable(currentMessage);
	}

	/**
	 * Create a message to be read.
	 * @param currentMessage The message to be read.
	 */
	public void setCurrentMessage(String currentMessage) {
		this.currentMessage = currentMessage;
	}

	/**
	 * The room the user is in.
	 * @return The room the user is in.
	 */
	public Room currentRoom() {
		return currentRoom;
	}

	/**
	 * Change the current room.
	 * @param currentRoom The room to change to.
	 */
	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}

	/**
	 * Whether the game is ended.
	 * @return {@code true} if the game has ended, otherwise {@code false}.
	 */
	public boolean isEnded() {
		return this.ended;
	}

	/**
	 * End the game.
	 */
	public void end() {
		this.ended = true;
	}

	/**
	 * Write current state to a file.
	 */
	public void write() {
		var delta = System.currentTimeMillis() - this.startTime;
		this.time = this.time.minusMillis(delta);
		updateHighScore();
		DataWriter.writeGameState();
	}
}
