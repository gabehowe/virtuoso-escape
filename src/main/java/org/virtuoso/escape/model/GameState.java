package org.virtuoso.escape.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.data.DataLoader;
import org.virtuoso.escape.model.data.DataWriter;

import java.time.Duration;
import java.util.*;

/**
 * @author Andrew
 */
public class GameState {
	private static GameState instance;
	private Floor currentFloor;
	private Room currentRoom;
	private Entity currentEntity;
	private Set<Item> currentItems;
	private Duration time;
	private long startTime;
	private Account account;
	private boolean ended;

	private Difficulty difficulty;
	private String currentMessage;

	public static GameState instance() {
		if (instance == null)
			instance = new GameState();
		return instance;
	}

	private GameState() {
	}

	public void begin(Account account) {
		JSONObject gameStateInfo = DataLoader.loadGameState(account);
		if (gameStateInfo == null) throw new RuntimeException("Couldn't find game state for " + account.id());
		this.currentFloor = GameInfo.instance().building().get((int) gameStateInfo.getOrDefault("currentFloor", 0));
		this.currentRoom = currentFloor.rooms().get((int) gameStateInfo.getOrDefault("currentRoom", 0));
		Object getEntity = gameStateInfo.getOrDefault("currentEntity", null);
		this.currentEntity = (getEntity != null) ? currentRoom.entities().get((int) getEntity) : null;
		this.currentItems = new HashSet<>();
		JSONArray items = (JSONArray) gameStateInfo.getOrDefault("currentItems", new JSONArray());
		for (int i = 0; i < items.size(); i++) {
			currentItems.add(Item.valueOf((String) items.get(i)));
		}
		this.time = Duration.ofSeconds((int) gameStateInfo.getOrDefault("time", 2700));
		this.account = account;
		this.difficulty = Difficulty.valueOf( (String) gameStateInfo.getOrDefault(difficulty, "SUBSTANTIAL"));
		this.startTime = System.currentTimeMillis();
	}

	public void pickEntity(Entity entity) {
		this.currentEntity = entity;
	}

	public void leaveEntity() {
		this.currentEntity = null;
	}

	public boolean hasItem(Item item) {
		return currentItems.contains(item);
	}

	public void addItem(Item item) {
		currentItems.add(item);
	}

	public Floor currentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(Floor currentFloor) {
		this.currentFloor = currentFloor;
		this.currentEntity = null;
		this.setCurrentRoom(currentFloor.rooms().getFirst());
	}

	public Optional<Entity> currentEntity() {
		return Optional.ofNullable(currentEntity);
	}

	public void setCurrentEntity(Entity currentEntity) {
		this.currentEntity = currentEntity;
	}


	public List<Item> currentItems() {
		return currentItems.stream().toList();
	}

	public Duration time() {
		var delta = System.currentTimeMillis() - this.startTime;
		return time.minusMillis(delta);
	}

	public void setTime(Duration time) {
		this.time = time;
	}

	public void addTime(Duration time) {
		this.time = this.time.plus(time);
	}

	public Account account() {
		return account;
	}

	public Difficulty difficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty diff) {
		this.difficulty = diff;
	}

	public Optional<String> currentMessage() {
		String currentMessage = this.currentMessage;
		this.currentMessage = null;
		return Optional.ofNullable(currentMessage);
	}

	public void setCurrentMessage(String currentMessage) {
		this.currentMessage = currentMessage;
	}

	public Room currentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}

	public boolean isEnded(){
		return this.ended;
	}
	public void end(){
		this.ended = true;
	}

	public void write() {
		var delta = System.currentTimeMillis() - this.startTime;
		this.time = this.time.minusMillis(delta);
		DataWriter.writeGameState();
	}
}
