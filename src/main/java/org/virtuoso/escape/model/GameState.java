package org.virtuoso.escape.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.virtuoso.escape.model.account.Account;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.account.Score;
import org.virtuoso.escape.model.data.DataLoader;
import org.virtuoso.escape.model.data.DataWriter;

import java.time.Duration;
import java.util.*;

/**
 * The current state of the game. Holds most mutable data.
 *
 * @author Andrew
 */
public class GameState {
    public static long initialTime = 2700;
    private static GameState instance;
    private Floor currentFloor;
    private Room currentRoom;
    private Entity currentEntity;
    private Set<Item> currentItems;
    private Duration time;
    private long startTime;
    private Account account;
    private boolean ended;
    private int penalty;
    private Map<String, Integer> hintsUsed;
    private Set<String> completedPuzzles;
    private Difficulty difficulty;
    private String currentMessage;

    /**
     * Prevents GameState from being constructed.
     */
    private GameState() {
    }

    /**
     * The global singleton.
     *
     * @return The global singleton.
     */
    public static GameState instance() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    /**
     * Initialize the singleton with data.
     *
     * @param account The account to draw data with.
     */
    public void begin(Account account) {
        JSONObject gameStateInfo = (JSONObject)
                DataLoader.loadGameStates().getOrDefault(account.id().toString(), new JSONObject());
        this.currentFloor = GameInfo.instance().building().stream()
                                    .filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentFloor")))
                                    .findFirst()
                                    .orElse(GameInfo.instance().building().getFirst());
        this.currentRoom = currentFloor.rooms().stream()
                                       .filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentRoom")))
                                       .findFirst()
                                       .orElse(currentFloor.rooms().getFirst());
        this.currentEntity = currentRoom.entities().stream()
                                        .filter(i -> Objects.equals(i.id(), gameStateInfo.get("currentEntity")))
                                        .findFirst()
                                        .orElse(null);

        this.currentItems = new HashSet<>();
        JSONArray items = (JSONArray) gameStateInfo.getOrDefault("currentItems", new JSONArray());
        for (Object item : items) this.currentItems.add(Item.valueOf((String) item));

        this.completedPuzzles = new HashSet<>();
        JSONArray completedPuzzlesJSON = (JSONArray) gameStateInfo.getOrDefault("completedPuzzles", new JSONArray());
        for (Object o : completedPuzzlesJSON) this.completedPuzzles.add((String) o);

        this.hintsUsed = new HashMap<>();
        JSONObject hintsUsed = (JSONObject) gameStateInfo.getOrDefault("hintsUsed", new JSONObject());
        for (Object level : hintsUsed.keySet()) this.hintsUsed.put(String.valueOf(level), ((Long) hintsUsed.get(level)).intValue());

        this.time = Duration.ofSeconds((Long) gameStateInfo.getOrDefault("time", initialTime));
        this.penalty = (int) gameStateInfo.getOrDefault("penalty", 0);
        this.account = account;
        this.difficulty = Difficulty.valueOf((String) gameStateInfo.getOrDefault(difficulty, "SUBSTANTIAL"));
        this.startTime = System.currentTimeMillis();
        JSONObject states = (JSONObject) gameStateInfo.getOrDefault("currentEntityStates", new JSONObject());
        for (Room room : currentFloor.rooms())
            for (Entity entity : room.entities())
                if (states.containsKey(entity.id())) entity.swapState((String) states.get(entity.id()));
    }

    /**
     * Focus {@code entity}
     *
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
     *
     * @param item The item to check for.
     * @return {@code true} if the inventory contains the item, otherwise {@code false}.
     */
    public boolean hasItem(Item item) {
        return currentItems.contains(item);
    }

    /**
     * Add an {@link Item} to the user's inventory.
     *
     * @param item The {@link Item} to add.
     */
    public void addItem(Item item) {
        if (item != null) currentItems.add(item);
    }

    /**
     * The current floor.
     *
     * @return The current floor.
     */
    public Floor currentFloor() {
        return currentFloor;
    }

    /**
     * Change to another floor.
     *
     * @param currentFloor The floor to change to.
     */
    public void setCurrentFloor(Floor currentFloor) {
        this.currentFloor = currentFloor;
        this.currentEntity = null;
        this.setCurrentRoom(currentFloor.rooms().getFirst());
    }

    /**
     * The currently focused entity.
     *
     * @return The {@link Optional<Entity>} if the current entity is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<Entity> currentEntity() {
        return Optional.ofNullable(currentEntity);
    }

    /**
     * Set the currently focused entity.
     *
     * @param currentEntity The entity to focus.
     */
    public void setCurrentEntity(Entity currentEntity) {
        this.currentEntity = currentEntity;
    }

    /**
     * The full inventory of the user.
     *
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
     *
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
     * Get the current penalty score.
     *
     * @return The current penalty score.
     */
    public int penalty() {
        return this.penalty;
    }

    /**
     * Add a penalty to the score.
     *
     * @param penalty The amount of penalty to add.
     */
    public void addPenalty(int penalty) {
        this.penalty += penalty;
    }

    /**
     * Increments the {@code initialTime} by 1 minute if it is less than 2 hours.
     */
    public void incrementInitialTime() {
        if (initialTime < 7200) initialTime += 60;
    }

    /**
     * The account logged in with.
     *
     * @return The account logged in with.
     */
    public Account account() {
        return account;
    }

    /**
     * The current difficulty.
     *
     * @return The current difficulty.
     */
    public Difficulty difficulty() {
        return difficulty;
    }

    /**
     * Set the current difficulty.
     *
     * @param diff The difficulty to set to.
     */
    public void setDifficulty(Difficulty diff) {
        this.difficulty = diff;
    }

    /**
     * Recalculate the high score and reset GameState.
     */
    public void updateHighScore() {
        if (isEnded()) {
            Long currentScore = Score.calculateScore(this.time, this.difficulty);
            Long oldScore = this.account.highScore().totalScore();
            if (oldScore == null || currentScore > oldScore) {
                this.account.setHighScore(new Score(this.time, this.difficulty, currentScore));
            }
        }
        AccountManager.instance().gameStates().remove(this.account().id());
        this.begin(this.account());
    }

    /**
     * Consume the current message.
     *
     * @return {@link Optional<String>} if the message is non-null, otherwise {@link Optional#empty()}.
     */
    public Optional<String> currentMessage() {
        String currentMessage = this.currentMessage;
        this.currentMessage = null;
        return Optional.ofNullable(currentMessage);
    }

    /**
     * Create a message to be read.
     *
     * @param currentMessage The message to be read.
     */
    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    /**
     * The room the user is in.
     *
     * @return The room the user is in.
     */
    public Room currentRoom() {
        return currentRoom;
    }

    /**
     * Change the current room.
     *
     * @param currentRoom The room to change to.
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
        this.currentEntity = null;
    }

    /**
     * Get the map of how many hints have been used on each level.
     *
     * @return The map of hint totals.
     */
    public Map<String, Integer> hintsUsed() {
        return this.hintsUsed;
    }

    /**
     * Sets the quantity of hints used for a certain level
     *
     * @param puzzle    The name of the puzzle the hint was used on.
     * @param hintsUsed The amount of hints used on that puzzle.
     */
    public void setHintsUsed(String puzzle, int hintsUsed) {
        this.hintsUsed().put(puzzle, hintsUsed);
    }

    /**
     * Get the list of completed puzzles.
     *
     * @return The list of completed puzzles.
     */
    public Set<String> completedPuzzles() {
        return this.completedPuzzles;
    }

    /**
     * Adds a puzzle to the completed puzzles set.
     *
     * @param puzzle The puzzle that was completed.
     */
    public void addCompletedPuzzle(String puzzle) {
        this.completedPuzzles.add(puzzle);
    }

    /**
     * Whether the game is ended.
     *
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
