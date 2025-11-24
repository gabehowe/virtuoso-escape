package org.virtuoso.escape.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.virtuoso.escape.model.account.*;
import org.virtuoso.escape.model.data.DataLoader;
import org.virtuoso.escape.model.data.DataWriter;

/**
 * Tests for GameState.java
 *
 * @author Bos
 */
public class GameStateTests {

    private GameState state;
    private Account account;

    @BeforeEach
    void setup() throws Exception {
        // Reset the singleton
        Field instanceField = GameState.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        state = GameState.instance();
        account = new Account("dummy", "dummy");
        injectPrivateField(state, "account", account);
        injectPrivateField(state, "time", Duration.ofSeconds(1000));
        injectPrivateField(state, "penalty", 0);
        injectPrivateField(state, "ended", false);
        injectPrivateField(state, "hintsUsed", new HashMap<>());
        injectPrivateField(state, "completedPuzzles", new HashSet<>());
        injectPrivateField(state, "currentItems", new HashSet<>());
        injectPrivateField(state, "difficulty", Difficulty.SUBSTANTIAL);
        injectPrivateField(state, "startTime", System.currentTimeMillis());
        injectPrivateField(state, "currentEntity", null);
    }

    private static void injectPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field f = instance.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(instance, value);
    }

    private static void injectStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    @Test
    void testSingletonIdentity() {
        GameState s1 = GameState.instance();
        GameState s2 = GameState.instance();
        assertSame(s1, s2);
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
