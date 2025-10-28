package org.virtuoso.escape.model.action;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.TemplateInvocationValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.DataLoader;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                Arguments.of(Difficulty.VIRTUOSIC, Severity.HIGH,     5 * 3 * 3),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.MEDIUM,   5 * 3 * 2),
                Arguments.of(Difficulty.VIRTUOSIC, Severity.LOW,      5 * 3 * 1),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.HIGH,   5 * 2 * 3),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.MEDIUM, 5 * 2 * 2),
                Arguments.of(Difficulty.SUBSTANTIAL, Severity.LOW,    5 * 2 * 1),
                Arguments.of(Difficulty.TRIVIAL, Severity.HIGH,       5 * 1 * 3),
                Arguments.of(Difficulty.TRIVIAL, Severity.MEDIUM,     5 * 1 * 2),
                Arguments.of(Difficulty.TRIVIAL, Severity.LOW,        5 * 1 * 1)
        );
    }

}
