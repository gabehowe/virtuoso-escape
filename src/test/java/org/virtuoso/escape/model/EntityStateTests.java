package org.virtuoso.escape.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.virtuoso.escape.model.action.*;
import org.virtuoso.escape.model.data.DataLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Andrew
 */
public class EntityStateTests {
    GameProjection proj;



    @BeforeEach
    public void pre() {
        proj = new GameProjection();
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        assertTrue(proj.login("dummy", "dummy"));
    }

    // -- Constructor Tests -- //
    @DisplayName("Should construct and actionless entity state from an id")
    @Test
    public void constructActionlessStateTest() {
        EntityState state = new EntityState("testState");

        assertEquals("testState", state.id(), "ID should be set correctly");
        assertTrue(state.capabilities().attack(), "Attack capability should be true");
        assertTrue(state.capabilities().inspect(), "Inspect capability should be true");
        assertTrue(state.capabilities().interact(), "Interact capability should be true");
        assertFalse(state.capabilities().input(), "Input capability should be false");

        assertNotNull(state.attackAction(), "Attack action should not be null");
        assertNotNull(state.inspectAction(), "Inspect action should not be null");
        assertNotNull(state.interactAction(), "Interact action should not be null");
        assertNull(state.inputAction(), "Input action should be null");
    }

    @DisplayName("Should throw an error when constructed with a null id")
    @Test
    public void constructStateNullIdTest() {
        assertThrows(NullPointerException.class, () -> {
            new EntityState(null);
        });
    }

    @DisplayName("Should construct an entity state with 5 parameters")
    @Test
    void constructState5Param() {
        Action attack = new AddPenalty(Severity.HIGH);
        Action inspect = new SetFloor(2);
        Action interact = new SetMessage("TestMessage");
        TakeInput input = new TakeInput();

        EntityState state = new EntityState("testState", attack, inspect, interact, input);

        assertEquals("testState", state.id());
        assertSame(attack, state.attackAction());
        assertSame(inspect, state.inspectAction());
        assertSame(interact, state.interactAction());
        assertSame(input, state.inputAction());

        assertTrue(state.capabilities().attack());
        assertTrue(state.capabilities().inspect());
        assertTrue(state.capabilities().interact());
        assertTrue(state.capabilities().input());
    }

    @DisplayName("Should construct an entity state with some null parameters")
    @Test
    void constructStateSomeNullParam() {
        Action attack = new AddPenalty(Severity.HIGH);
        Action interact = new SetMessage("TestMessage");

        EntityState state = new EntityState("testState", attack, null, interact, null);

        assertEquals("testState", state.id());
        assertSame(attack, state.attackAction());
        assertNull(state.inspectAction());
        assertSame(interact, state.interactAction());
        assertNull(state.inputAction());

        assertTrue(state.capabilities().attack());
        assertFalse(state.capabilities().inspect());
        assertTrue(state.capabilities().interact());
        assertFalse(state.capabilities().input());
    }

    @DisplayName("Should construct an entity state with all null parameters")
    @Test
    void constructStateAllNullParam() {
        EntityState state = new EntityState("testState", null, null, null, null);

        assertEquals("testState", state.id());
        assertNull(state.attackAction());
        assertNull(state.inspectAction());
        assertNull(state.interactAction());
        assertNull(state.inputAction());

        assertFalse(state.capabilities().attack());
        assertFalse(state.capabilities().inspect());
        assertFalse(state.capabilities().interact());
        assertFalse(state.capabilities().input());
    }

    @DisplayName("Should construct an entity state using the 6-parameter canonical constructor")
    @Test
    void constructState6Param() {
        Action attack = new AddPenalty(Severity.HIGH);
        Action inspect = new SetFloor(2);
        Action interact = new SetMessage("InteractMessage");
        TakeInput input = new TakeInput();
        EntityState.Capabilities capabilities = new EntityState.Capabilities(true, false, false, true);

        EntityState state = new EntityState("testState", attack, inspect, interact, input, capabilities);

        assertEquals("testState", state.id());
        assertSame(attack, state.attackAction());
        assertSame(inspect, state.inspectAction());
        assertSame(interact, state.interactAction());
        assertSame(input, state.inputAction());
        assertSame(capabilities, state.capabilities());

        assertTrue(state.capabilities().attack());
        assertFalse(state.capabilities().inspect());
        assertFalse(state.capabilities().interact());
        assertTrue(state.capabilities().input());
    }

    // -- Get Text Tests -- //
    @ParameterizedTest(name = "Will get text from entity state: Test {index} => key: {0}, expected: {1}")
    @MethodSource
    public void getTextTest(String key, String expected) throws Exception {
        EntityState state = new EntityState("welcome");

        String result = getText(state, key);

        assertEquals(expected, result);
    }

    private static Stream<Arguments> getTextTest() {
        return Stream.of(
                Arguments.of("welcome", "Welcome to Virtuoso Escape!"),
                Arguments.of("missingText", "<welcome/missingText>"),
                Arguments.of(null, "<welcome/null>"));
    }

    private static String getText(EntityState state, String key)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getTextMethod = EntityState.class.getDeclaredMethod("getText", String.class);
        getTextMethod.setAccessible(true);
        return (String) getTextMethod.invoke(state, key);
    }

    // -- Action Tests -- //
    @ParameterizedTest(
            name =
                    "Will call actions through interact, attack and inspect : Test {index} => method: {0}, key: {1}, hasAction: {2}, nullAction: {3}")
    @MethodSource
    void actionCallTest(String methodName, String key, boolean hasAction, boolean nullAction) throws Exception {
        Action mockAction;
        AtomicBoolean executed = new AtomicBoolean(false);
        mockAction = nullAction ? () -> executed.set(true) : null;

        EntityState testState =
                switch (methodName) {
                    case "interact" -> new EntityState(
                            "testState",
                            null,
                            null,
                            mockAction,
                            null,
                            new EntityState.Capabilities(false, false, hasAction, false));
                    case "attack" -> new EntityState(
                            "testState",
                            mockAction,
                            null,
                            null,
                            null,
                            new EntityState.Capabilities(hasAction, false, false, false));
                    case "inspect" -> new EntityState(
                            "testState",
                            null,
                            mockAction,
                            null,
                            null,
                            new EntityState.Capabilities(false, hasAction, false, false));
                    default -> throw new IllegalArgumentException("Unknown method: " + methodName);
                };

        GameState.instance().setCurrentMessage(null);

        Method method = EntityState.class.getMethod(methodName);
        if (!hasAction) {
            assertThrows(Exception.class, () -> method.invoke(testState));
            return;
        } else method.invoke(testState);

        String expectedMessage = getText(testState, key);

        assertEquals(expectedMessage, GameState.instance().currentMessage().get());
        assertEquals(nullAction, executed.get());
    }

    private static Stream<Arguments> actionCallTest() {
        return Stream.of(
                Arguments.of("interact", "interact", true, false),
                Arguments.of("interact", "interact", false, false),
                Arguments.of("attack", "attack", true, false),
                Arguments.of("attack", "attack", false, false),
                Arguments.of("inspect", "inspect", true, false),
                Arguments.of("inspect", "inspect", false, false),
                Arguments.of("interact", "interact", true, true),
                Arguments.of("interact", "interact", false, true),
                Arguments.of("attack", "attack", true, true),
                Arguments.of("attack", "attack", false, true),
                Arguments.of("inspect", "inspect", true, true),
                Arguments.of("inspect", "inspect", false, true));
    }

    // -- Name Tests -- //
    @DisplayName("Should return the entity's name")
    @Test
    void nameMethodTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        EntityState state = new EntityState("intro_squirrel");
        String name = state.name();
        String expectedName = getText(state, "name");
        assertEquals(expectedName, name);
    }

    @DisplayName("Should return fallback for name if missing")
    @Test
    void nameMethodMissingTest() {
        EntityState state = new EntityState("missingName");
        String name = state.name();
        assertEquals("<missingName/name>", name);
    }

    // -- Introduce Tests -- //
    @DisplayName("Should display the introductory message")
    @Test
    void introduceMethodTest() throws Exception {
        EntityState state = new EntityState("intro_squirrel");
        state.introduce();
        String expectedMessage = getText(state, "introduce");
        assertEquals(expectedMessage, GameState.instance().currentMessage().get());
    }

    @DisplayName("Should handle introduce with missing text gracefully")
    @Test
    void introduceMethodMissingTest() {
        EntityState state = new EntityState("missingIntroduce");
        state.introduce();
        String message = GameState.instance().currentMessage().get();
        assertEquals("<missingIntroduce/introduce>", message);
    }

    // -- Equals Tests -- //
    @DisplayName("Should correctly compare equality of two entity states by id")
    @Test
    void equalsMethodTest() {
        EntityState state1 = new EntityState("testState");
        EntityState state2 = new EntityState("testState");
        EntityState state3 = new EntityState("otherState");

        assertTrue(state1.equals(state2));
        assertTrue(state1.equals(state1));
        assertFalse(state1.equals(state3));
    }

    // -- id Tests -- //
    @DisplayName("Should return the correct id")
    @Test
    void idMethodTest() {
        EntityState state = new EntityState("testState");
        String result = state.id();
        assertEquals("testState", result);
    }

    // -- Take Input Tests -- //
    @DisplayName("Should handle takeInput correctly when input action exists")
    @Test
    void takeInputActionExistsTest() {
        AtomicBoolean executed = new AtomicBoolean(false);
        TakeInput inputAction = new TakeInput("", TakeInput.makeCases("cd", (Action) () -> executed.set(true)));

        EntityState state = new EntityState("computty_unblocked", null, null, null, inputAction);
        state.takeInput("cd");

        assertTrue(executed.get());
        assertTrue(GameState.instance().currentMessage().get().equals(""));
    }

    @DisplayName("Should handle takeInput correctly when input action is null")
    @Test
    void takeInputActionNullTest() {
        EntityState state = new EntityState("testState", null, null, null, null, new EntityState.Capabilities(false, false, false, true));
        state.takeInput("unknownInput");
        String message = GameState.instance().currentMessage().get();
        assertTrue(message.contains("I couldn't understand 'unknownInput'"));
    }

    @DisplayName("Should handle takeInput when input action is null and key exists")
    @Test
    void takeInputNullActionKeyExistsTest() {
        EntityState state = new EntityState("computty_unblocked", null, null, null, null, new EntityState.Capabilities(false, false, false, true));
        state.takeInput("cd");
        String message = GameState.instance().currentMessage().get();
        assertEquals(GameInfo.instance().string("computty_unblocked", "input_cd"), message);
    }
}
