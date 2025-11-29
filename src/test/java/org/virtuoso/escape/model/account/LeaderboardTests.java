package org.virtuoso.escape.model.account;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.virtuoso.escape.model.*;

/**
 * tests for Leaderboard.java
 *
 * @author Bose
 */
public class LeaderboardTests {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    private GameState mockGameState;
    private Account mockAccount;
    private AccountManager mockAccountManager;

    private JSONObject fakeAccountsJson;

    @BeforeEach
    void setup() throws Exception {
        Util.rebuildSingleton(GameState.class);
        Util.rebuildSingleton(GameInfo.class);
        Util.rebuildSingleton(AccountManager.class);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        mockAccount = new Account(
                "dummy",
                "dummy",
                UUID.randomUUID(),
                new Score(Duration.ofSeconds(100), Difficulty.TRIVIAL, 100L),
                false);

        mockGameState = GameState.instance();

        injectPrivateField(GameState.class, "account", mockGameState, mockAccount);
        injectPrivateField(GameState.class, "difficulty", mockGameState, Difficulty.SUBSTANTIAL);
        injectPrivateField(GameState.class, "time", mockGameState, Duration.ofSeconds(200));

        // Dummy build
        fakeAccountsJson = new JSONObject();
        fakeAccountsJson.putAll(buildAccountJson("dummy", "TRIVIAL", 100L, 100L));

        mockAccountManager = AccountManager.instance();
        mockAccountManager.accounts().putAll(fakeAccountsJson);
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }

    private static void injectPrivateField(Class<?> clazz, String fieldName, Object instance, Object value)
            throws Exception {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);

        f.set(instance, value);
    }

    private static JSONObject buildAccountJson(String username, String difficulty, Long timeRem, Long score) {
        JSONObject hs = new JSONObject();
        hs.put("difficulty", difficulty);
        hs.put("timeRemaining", timeRem);
        hs.put("totalScore", score);

        JSONObject acct = new JSONObject();
        acct.put("username", username);
        acct.put("highScore", hs);

        JSONObject container = new JSONObject();
        container.put(UUID.randomUUID().toString(), acct);
        return container;
    }

    @Test
    @DisplayName("recordSession should update high score if new score is higher")
    void testRecordSessionHigherScore() {
        long old = mockAccount.highScore().totalScore();
        Leaderboard.recordSession("dummy");
        long updated = mockAccount.highScore().totalScore();
        assertTrue(updated >= old, "High score should increase or stay same if higher");
    }

    @Test
    @DisplayName("recordSession should not update when new score is lower")
    void testRecordSessionLowerScore() throws Exception {
        injectPrivateField(GameState.class, "time", mockGameState, Duration.ofSeconds(1));
        long old = mockAccount.highScore().totalScore();
        Leaderboard.recordSession("dummy");
        assertEquals(old, mockAccount.highScore().totalScore(), "Should not overwrite with lower score");
    }

    @Test
    @DisplayName("Leaderboard should format time correctly as MM:SS")
    void testFormattedTime() {
        Leaderboard.ScoreEntry entry = new Leaderboard.ScoreEntry("user", 999L, 125L, "TRIVIAL");
        assertEquals("02:05", entry.getFormattedTime());
    }

    @Test
    @DisplayName("Leaderboard should print correctly with single user")
    void testShowLeaderboardSingle() {
        Leaderboard.showLeaderboard();
        String output = outContent.toString();
        assertTrue(output.contains("Top 5 Leaderboard"));
        assertTrue(output.contains("USERNAME"));
        assertTrue(output.contains("dummy"));
    }

    @Test
    @DisplayName("Leaderboard should handle empty AccountManager gracefully")
    void testShowLeaderboardEmpty() throws Exception {
        mockAccountManager.accounts().putAll(new JSONObject());
        assertDoesNotThrow(Leaderboard::showLeaderboard);
    }

    @Test
    @DisplayName("Leaderboard should handle multiple users and sort correctly")
    void testLeaderboardSorting() throws Exception {
        JSONObject multiple = new JSONObject();
        multiple.putAll(buildAccountJson("low", "TRIVIAL", 50L, 50L));
        multiple.putAll(buildAccountJson("mid", "SUBSTANTIAL", 120L, 200L));
        multiple.putAll(buildAccountJson("high", "VIRTUOSIC", 300L, 1000L));
        multiple.putAll(buildAccountJson("tieA", "SUBSTANTIAL", 60L, 500L));
        multiple.putAll(buildAccountJson("tieB", "VIRTUOSIC", 90L, 500L));
        mockAccountManager.accounts().clear();
        mockAccountManager.accounts().putAll(multiple);

        Leaderboard.showLeaderboard();
        String output = outContent.toString();
        var lines = List.of(output.split("\n"));

        // checking order
        int idxHigh = lines.indexOf(
                lines.stream().filter(it -> it.contains("high")).findFirst().get());
        int idxTieA = output.indexOf(
                lines.stream().filter(it -> it.contains("tieA")).findFirst().get());
        int idxLow = output.indexOf(
                lines.stream().filter(it -> it.contains("low")).findFirst().get());

        assertTrue(idxHigh < idxLow, "High scorer should appear before low scorer");
        assertTrue(idxTieA < idxLow, "Tie players should appear before low scorer");
        assertTrue(output.contains("Top 5 Leaderboard"));
    }

    @Test
    @DisplayName("Leaderboard should skip entries with missing fields safely")
    void testMissingFields() throws Exception {
        JSONObject incomplete = new JSONObject();
        JSONObject user = new JSONObject();
        user.put("username", "broken");
        // Missing highscore and missing timeremain
        incomplete.put(UUID.randomUUID().toString(), user);

        mockAccountManager.accounts().putAll(incomplete);

        assertDoesNotThrow(Leaderboard::showLeaderboard);
        String out = outContent.toString();
        assertTrue(out.contains("Top 5 Leaderboard"));
    }

    @Test
    @DisplayName("Leaderboard should handle null totalScore values safely")
    void testNullTotalScore() throws Exception {
        JSONObject corrupted = new JSONObject();
        JSONObject hs = new JSONObject();
        hs.put("difficulty", "TRIVIAL");
        hs.put("timeRemaining", 100L);
        hs.put("totalScore", null);

        JSONObject acct = new JSONObject();
        acct.put("username", "nullscore");
        acct.put("highScore", hs);
        corrupted.put(UUID.randomUUID().toString(), acct);

        mockAccountManager.accounts().putAll(corrupted);
        assertDoesNotThrow(Leaderboard::showLeaderboard);
    }
}
