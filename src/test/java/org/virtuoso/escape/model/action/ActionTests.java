package org.virtuoso.escape.model.action;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gabri
 */
public class ActionTests {
    GameProjection proj;

    @BeforeEach
    public void pre() {
        proj = new GameProjection();
        DataLoader.ACCOUNTS_PATH = getClass().getResource("accounts.json").getPath();
        DataLoader.GAMESTATES_PATH = getClass().getResource("gamestates.json").getPath();
        assertTrue(proj.login("dummy", "dummy"));
    }

    @AfterEach
    public void post() {
        //        proj.logout();
    }

    @DisplayName("Should apply penalty based on difficulty and severity")
    @ParameterizedTest
    @MethodSource
    public void addPenaltyTest(Difficulty difficulty, Severity severity, int expectedDifference) {
        var pre = GameState.instance().penalty();
        var expectedPenalty = pre - expectedDifference;
        proj.setDifficulty(difficulty);
        new AddPenalty(severity).execute();
        assertEquals(GameState.instance().penalty(), expectedPenalty);
    }

    private static Stream<Arguments> addPenaltyTest() {
        return Stream.of(
                Arguments.of(Difficulty.VIRTUOSIC, Severity.HIGH, 5 * 3 * 3),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.MEDIUM, 5 * 3 * 2),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.LOW, 5 * 3 * 1),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.HIGH, 5 * 2 * 3),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.MEDIUM, 5 * 2 * 2),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.LOW, 5 * 2 * 1),
                Arguments.of(Difficulty.TRIVIAL, Severity.HIGH, 5 * 1 * 3),
                Arguments.of(Difficulty.TRIVIAL, Severity.MEDIUM, 5 * 1 * 2),
                Arguments.of(Difficulty.TRIVIAL, Severity.LOW, 5 * 1 * 1));
    }

    // @formatter:on

    @DisplayName("Should execute actions in sequence and use the last action's result")
    @Test
    public void chainTest() {
        new Chain(new SetMessage("initial"), new SetMessage("expected")).execute();
        assertEquals("expected", GameState.instance().currentMessage().get());
    }

    @DisplayName("Should execute correct action branch based on condition")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void conditionalTest(boolean value) {
        new Conditional(() -> value, new SetMessage(String.valueOf(true)), new SetMessage(String.valueOf(false)))
                .execute();
        assertEquals(
                String.valueOf(value), GameState.instance().currentMessage().get());
    }

    @DisplayName("Should handle null action branch when condition is false")
    @Test
    public void nullConditionalTest() {
        new Conditional(() -> false, new SetMessage(String.valueOf(true)), null).execute();
        assertFalse(GameState.instance().currentMessage().isPresent());
    }

    @DisplayName("Should execute default action without errors")
    @Test
    public void defaultTest() {
        new Default().execute();
        // Expect no error?
    }

    @DisplayName("Should execute action based on matching input case")
    @ParameterizedTest
    @ValueSource(strings = {"one", "two", "three"})
    public void testTakeInput(String input) {
        new TakeInput("one", new SetMessage("one"), "two", new SetMessage("two"), "three", new SetMessage("three"))
                .withInput(input)
                .execute();
        var msg = GameState.instance().currentMessage();
        assertTrue(msg.isPresent());
        assertEquals(input, msg.get());
    }

    @DisplayName("Should execute default action when input does not match any case")
    @Test
    public void testDefaultTakeInput() {
        new TakeInput("", new LinkedHashMap<>(), new SetMessage("default"))
                .withInput(null)
                .execute();
        var msg = GameState.instance().currentMessage();
        assertTrue(msg.isPresent());
        assertEquals("default", msg.get());
    }

    @DisplayName("Should handle null default action when input does not match any case")
    @Test
    public void testNullDefaultTakeInput() {
        new TakeInput("", new LinkedHashMap<>(), null).withInput(null).execute();
        var msg = GameState.instance().currentMessage();
        assertFalse(msg.isPresent());
    }

    @DisplayName("Should throw error for non-even TakeInput arguments")
    @Test
    public void testNonEvenTakeInputMakeCasesArgumentLength() {
        try {
            new TakeInput("one").execute();
        } catch (AssertionError | IndexOutOfBoundsException e) {
            return; // Passed!
        }
        fail("Error should have been thrown!");
    }
}
