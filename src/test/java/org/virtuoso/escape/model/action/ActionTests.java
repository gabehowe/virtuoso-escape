package org.virtuoso.escape.model.action;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameProjection;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.data.DataLoader;

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

    private void addPenaltyTest(Difficulty difficulty, Severity severity, int expectedDifference) {
        var pre = GameState.instance().penalty();
        var expectedPenalty = pre - expectedDifference;
        proj.setDifficulty(difficulty);
        new AddPenalty(severity).execute();
        assertEquals(GameState.instance().penalty(),expectedPenalty);
    }

    @Test
    public void highVirtuosicPenalty() {
        addPenaltyTest(Difficulty.VIRTUOSIC, Severity.HIGH, 5 * 3 * 3);
    }

    @Test
    public void mediumVirtuosicPenalty() {
        addPenaltyTest(Difficulty.VIRTUOSIC, Severity.MEDIUM, 5 * 2 * 3);
    }

    @Test
    public void lowVirtuosicPenalty() {
        addPenaltyTest(Difficulty.VIRTUOSIC, Severity.LOW, 5 * 1 * 3);
    }

    @Test
    public void highSubstantialPenalty() {
        addPenaltyTest(Difficulty.SUBSTANTIAL, Severity.HIGH, 5 * 3 * 2);
    }

    @Test
    public void mediumSubstantialPenalty() {
        addPenaltyTest(Difficulty.SUBSTANTIAL, Severity.MEDIUM, 5 * 2 * 2);
    }

    @Test
    public void lowSubstantialPenalty() {
        addPenaltyTest(Difficulty.SUBSTANTIAL, Severity.LOW, 5 * 1 * 2);
    }

    @Test
    public void highTrivialPenalty() {
        addPenaltyTest(Difficulty.TRIVIAL, Severity.HIGH, 5 * 3 * 1);
    }

    @Test
    public void mediumTrivialPenalty() {
        addPenaltyTest(Difficulty.TRIVIAL, Severity.MEDIUM, 5 * 2 * 1);
    }

    @Test
    public void lowTrivialPenalty() {
        addPenaltyTest(Difficulty.TRIVIAL, Severity.LOW, 5 * 1 * 1);
    }
}
