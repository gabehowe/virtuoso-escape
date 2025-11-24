package org.virtuoso.escape.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.virtuoso.escape.model.account.AccountManager;
import org.virtuoso.escape.model.account.Score;
import org.virtuoso.escape.model.data.DataLoader;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameState.java
 *
 * @author Bos
 */
public class GameStateTests {

    private GameState state;
    private GameProjection proj;

    private static String stateData =
            """
                      {"7766f361-af7a-4da5-b741-6867d1768d45": {
                        "currentItems": [],
                        "difficulty": "SUBSTANTIAL",
                        "currentRoom": "acorn_grove_0",
                        "currentEntity": null,
                        "currentFloor": "acorn_grove",
                        "completedPuzzles": [],
                        "time": 2698,
                        "currentEntityStates": {
                          "narrator": "narrator_start",
                          "portal_squirrel": "portal_squirrel",
                          "intro_squirrel": "intro_squirrel"
                        },
                        "hintsUsed": {}
                      },
                    "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
                      "currentItems": ["keys"],
                      "difficulty": "SUBSTANTIAL",
                      "currentRoom": "acorn_grove_0",
                      "currentFloor": "acorn_grove",
                      "completedPuzzles": ["joe_hardy"],
                      "time": 2680,
                      "currentEntityStates": {
                        "narrator": "narrator_hint_1",
                        "portal_squirrel": "portal_squirrel",
                        "intro_squirrel": "intro_squirrel"
                      },
                      "hintsUsed": {
                        "acorn_grove": 1
                      }
                    },
                      }
                    """;
    private static String accountData =
            """
                      {
                      "7766f361-af7a-4da5-b741-6867d1768d45": {
                        "highScore": {
                          "difficulty": "TRIVIAL",
                          "timeRemaining": null,
                          "totalScore": null
                        },
                        "hashedPassword": "829c3804401b0727f70f73d4415e162400cbe57b",
                        "ttsOn": true,
                        "username": "dummy"
                      }
                    "d32ad7e6-570f-43cb-b447-82d4c8be293e": {
                      "highScore": {
                        "difficulty": "TRIVIAL",
                        "timeRemaining": null,
                        "totalScore": null
                      },
                      "ttsOn": true,
                      "hashedPassword": "829c3804401b0727f70f73d4415e162400cbe57b",
                      "username": "dummy_loaded"
                    },
                      }
                    """;

    @BeforeEach
    void setup() throws Exception {
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        try {
            Files.writeString(Path.of(DataLoader.ACCOUNTS_PATH), accountData);
            Files.writeString(Path.of(DataLoader.GAMESTATES_PATH), stateData);
        } catch (Exception e) {
            throw new RuntimeException("couldn't write to file!");
        }
        proj = new GameProjection();
        // Reset the singleton
        Field instanceField = GameState.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        state = GameState.instance();
        proj.login("dummy", "dummy");

//        injectPrivateField(state, "account", account);
        injectPrivateField(state, "time", Duration.ofSeconds(1000));
        injectPrivateField(state, "penalty", 0);
        injectPrivateField(state, "ended", false);
        injectPrivateField(state, "hintsUsed", new HashMap<>());
        injectPrivateField(state, "completedPuzzles", new HashSet<>());
        injectPrivateField(state, "currentItems", new HashSet<>(Set.of()));
        injectPrivateField(state, "difficulty", Difficulty.SUBSTANTIAL);
        injectPrivateField(state, "startTime", System.currentTimeMillis());
        injectPrivateField(state, "currentEntity", null);
    }

    private static void injectPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field f = instance.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(instance, value);
    }

    @Test
    void startWithLoadedData() throws Exception {
        Util.rebuildSingleton(GameState.class);
        Util.rebuildSingleton(AccountManager.class);
        var projection = new GameProjection();
        assertTrue(projection.login("dummy_loaded", "dummy"));
        assertTrue(projection.currentItems().contains(Item.keys));
        assertTrue(GameState.instance().completedPuzzles().contains("joe_hardy"));
    }

    @Test
    void testSingletonIdentity() {
        GameState s1 = GameState.instance();
        GameState s2 = GameState.instance();
        assertSame(s1, s2);
    }

    @ParameterizedTest
    @CsvSource({
            "50, 100",
            "150, 150",
            "100, 100"
    })
    void testUpdateHighScore(int value, int expected) {
        GameState.instance().end();
        GameState.instance().account().setHighScore(new Score(null, null, 100L));
        GameState.instance().setTime(Duration.ofSeconds(value));
        GameState.instance().updateHighScore();
        assertEquals(expected, GameState.instance().account().highScore().totalScore());

    }


    @Test
    void testAddAndClearItems() {
        Item keycard = Item.keys;
        state.addItem(keycard);
        assertTrue(state.hasItem(keycard));

        // Duplicate creatrion for test
        state.addItem(keycard);
        assertEquals(1, state.currentItems().size());

        state.clearItems();
        assertFalse(state.hasItem(keycard));
    }

    @Test
    void testPickAndSetEntity() {
        Entity e1 = new Entity("id1");
        state.pickEntity(e1);
        assertTrue(state.currentEntity().isPresent());
        assertEquals(e1, state.currentEntity().get());

        Entity e2 = new Entity("id2");
        state.setCurrentEntity(e2);
        assertEquals(e2, state.currentEntity().get());

        state.leaveEntity();
        assertTrue(state.currentEntity().isEmpty());
    }

    @Test
    void testTimeAndSetTime() throws InterruptedException {
        state.setTime(Duration.ofSeconds(2));
        Duration t1 = state.time();
        Thread.sleep(10);
        Duration t2 = state.time();
        assertTrue(t2.toMillis() < t1.toMillis() || t2.toMillis() == 0);
    }

    @Test
    void testAddPenalty() {
        state.addPenalty(5);
        assertEquals(5, state.penalty());
        state.addPenalty(10);
        assertEquals(15, state.penalty());
    }

    @Test
    void testIncrementInitialTimeBoundaries() throws Exception {
        Field f = GameState.class.getDeclaredField("initialTime");
        f.setAccessible(true);

        f.setLong(null, 7199L);
        state.incrementInitialTime();
        assertEquals(7259L, f.getLong(null));

        f.setLong(null, 7200L);
        state.incrementInitialTime();
        assertEquals(7200L, f.getLong(null));
    }

    @Test
    void testSetAndGetDifficulty() {
        state.setDifficulty(Difficulty.VIRTUOSIC);
        assertEquals(Difficulty.VIRTUOSIC, state.difficulty());
    }

    @Test
    void testCurrentMessageConsumption() {
        state.setCurrentMessage("hello");
        Optional<String> msg1 = state.currentMessage();
        assertTrue(msg1.isPresent());
        assertEquals("hello", msg1.get());

        Optional<String> msg2 = state.currentMessage();
        assertTrue(msg2.isEmpty());
    }

    // completed

    @Test
    void testAddCompletedPuzzleIdempotency() {
        state.addCompletedPuzzle("PuzzleA");
        state.addCompletedPuzzle("PuzzleA"); // duplicate
        assertEquals(1, state.completedPuzzles().size());
        assertTrue(state.completedPuzzles().contains("PuzzleA"));
    }

    @Test
    void testHintsUsed() {
        state.setHintsUsed("Level1", 2);
        assertEquals(2, state.hintsUsed().get("Level1"));
        assertNull(state.hintsUsed().get("Level2"));
    }

    // high svore
    @Test
    void testEndGame() {
        assertFalse(state.isEnded());
        state.end();
        assertTrue(state.isEnded());
    }


    @Test
    void testSetCurrentFloorResetsRoomAndEntity() throws Exception {
        Floor floor1 = new Floor("F1", new LinkedList<>());
        Floor floor2 = new Floor("F2", new LinkedList<>());
        injectPrivateField(state, "currentFloor", floor1);
        Room r1 = new Room("R1", new ArrayList<>(), "");
        floor1.rooms().add(r1);
        Room r2 = new Room("R2", new ArrayList<>(), "");
        floor2.rooms().add(r2);

        state.setCurrentFloor(floor2);
        assertEquals(floor2, state.currentFloor());
        assertEquals(r2, state.currentRoom());
        assertTrue(state.currentEntity().isEmpty());
    }

    @Test
    void testSetCurrentRoomDoesNotChangeFloor() throws Exception {
        Floor floor = new Floor("F", new LinkedList<>());
        Room r1 = new Room("R1", new ArrayList<>(), "");
        Room r2 = new Room("R2", new ArrayList<>(), "");
        floor.rooms().add(r1);
        floor.rooms().add(r2);
        injectPrivateField(state, "currentFloor", floor);
        state.setCurrentRoom(r2);
        assertEquals(floor, state.currentFloor());
        assertEquals(r2, state.currentRoom());
    }
}
