package org.virtuoso.escape.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.virtuoso.escape.model.action.Action;
import org.virtuoso.escape.model.action.SetMessage;
import org.virtuoso.escape.model.action.TakeInput;
import org.virtuoso.escape.model.data.DataLoader;

/** @author Andrew Heuer */
public class EntityTests {
    GameProjection proj;

    @BeforeEach
    public void pre() {
        proj = new GameProjection();
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        assertTrue(proj.login("dummy", "dummy"));
        GameState.instance().setCurrentMessage(null);
    }

    // -- Constructor Tests -- //
    @DisplayName("Should construct an actionless entity with default state")
    @Test
    void constructActionlessEntityTest() {
        Entity entity = new Entity("testEntity");

        assertEquals("testEntity", entity.id());
        assertNotNull(entity.state());
        assertEquals("testEntity", entity.state().id());
    }

    @DisplayName("Should construct an entity with multiple states and default to matching id")
    @Test
    void constructEntityMultipleStatesDefaultIdTest() {
        EntityState state1 = new EntityState("state1");
        EntityState state2 = new EntityState("state2");

        Entity entity = new Entity("state1", state2, state1);

        assertEquals("state1", entity.id());
        assertEquals("state1", entity.state().id());
    }

    @DisplayName("Should construct an entity with multiple states and default to first state if id missing")
    @Test
    void constructEntityMultipleStatesDefaultFirstStateTest() {
        EntityState state1 = new EntityState("state1");
        EntityState state2 = new EntityState("state2");

        Entity entity = new Entity("missingId", state1, state2);

        assertEquals("missingId", entity.id());
        assertEquals("state1", entity.state().id());
    }

    @DisplayName("Should construct a one-state entity with actions")
    @Test
    void constructEntityWithActionsTest() {
        AtomicBoolean attackExecuted = new AtomicBoolean(false);
        Action attack = () -> attackExecuted.set(true);
        Action inspect = new SetMessage("inspect");
        Action interact = new SetMessage("interact");
        TakeInput input = new TakeInput("", TakeInput.makeCases("cmd", (Action) () -> {}));

        Entity entity = new Entity("actionEntity", attack, inspect, interact, input);

        assertEquals("actionEntity", entity.id());
        assertNotNull(entity.state());
        assertNotNull(entity.state().attackAction());
        assertNotNull(entity.state().inspectAction());
        assertNotNull(entity.state().interactAction());
        assertNotNull(entity.state().inputAction());

        entity.state().attackAction().execute();
        assertTrue(attackExecuted.get());
    }

    @DisplayName("Constructing an entity with a null id should throw NullPointerException")
    @Test
    void constructWithNullIdTest() {
        assertThrows(NullPointerException.class, () -> {
            new Entity(null);
        });
    }

    // -- State Swap Tests -- //
    @DisplayName("Should swap states correctly for multiple scenarios")
    @ParameterizedTest(name = "Swap from {0} to {1}")
    @MethodSource
    void swapStateTests(String initialState, String newState, String expected) {
        EntityState state1 = new EntityState("state1");
        EntityState state2 = new EntityState("state2");
        Entity entity = new Entity("state1", state1, state2);

        entity.swapState(newState);
        assertEquals(expected, entity.state().id());
    }

    private static Stream<Arguments> swapStateTests() {
        return Stream.of(Arguments.of("state1", "state2", "state2"), Arguments.of("state1", "state1", "state1"));
    }

    @DisplayName("Swapping to a nonexistent state should throw an exception")
    @Test
    void swapNonExistentState() {
        EntityState state1 = new EntityState("state1");
        Entity entity = new Entity("myEntity", state1);

        assertThrows(RuntimeException.class, () -> {
            entity.swapState("nonexistent");
        });
    }

    // -- Write Method Tests -- //
    @DisplayName("Should return id and current state in write()")
    @Test
    void writeMethodTest() {
        EntityState state1 = new EntityState("state1");
        EntityState state2 = new EntityState("state2");

        Entity entity = new Entity("state1", state1, state2);
        entity.swapState("state2");

        String[] result = entity.write();
        assertEquals(2, result.length);
        assertEquals("state1", result[0]);
        assertEquals("state2", result[1]);
    }

    // -- id Tests -- //
    @DisplayName("Should return the correct id")
    @Test
    void idMethodTest() {
        Entity entity = new Entity("testState");
        String result = entity.id();
        assertEquals("testState", result);
    }

    // -- State Retrieval Tests -- //
    @DisplayName("stateW should return the current state")
    @Test
    void stateRetrievalTest() {
        EntityState state = new EntityState("myState");
        Entity entity = new Entity("myEntity", state);

        assertEquals("myState", entity.state().id());
    }
}
