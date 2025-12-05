package org.virtuoso.escape.model.account;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.virtuoso.escape.model.Difficulty;
import org.virtuoso.escape.model.GameInfo;
import org.virtuoso.escape.model.GameState;
import org.virtuoso.escape.model.Util;

/**
 * Tests for Leaderboard.java
 *
 * @author Bose
 * @author Andrew
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
    @DisplayName("Should update high score when new score is higher")
    void testRecordSessionUpdatesHigherScore() {
        long old = mockAccount.highScore().totalScore();
        Leaderboard.recordSession("dummy");
        assertTrue(mockAccount.highScore().totalScore() >= old);
    }

    @Test
    @DisplayName("Should not update high score when new score is lower")
    void testRecordSessionDoesNotOverwriteWithLowerScore() throws Exception {
        injectPrivateField(GameState.class, "time", mockGameState, Duration.ofSeconds(1));
        long old = mockAccount.highScore().totalScore();
        Leaderboard.recordSession("dummy");
        assertEquals(old, mockAccount.highScore().totalScore());
    }

    @Test
    @DisplayName("Should correctly format time as MM:SS")
    void testFormattedTime() {
        Leaderboard.ScoreEntry entry = new Leaderboard.ScoreEntry("user", 999L, 125L, "TRIVIAL");
        assertEquals("02:05", entry.getFormattedTime());
    }

    @Test
    @DisplayName("Should print leaderboard correctly with single user")
    void testShowLeaderboardSingleUser() {
        Leaderboard.showLeaderboard();
        String output = outContent.toString();
        assertTrue(output.contains("Top 5 Leaderboard"));
        assertTrue(output.contains("USERNAME"));
        assertTrue(output.contains("dummy"));
    }

    @Test
    @DisplayName("Should not throw when AccountManager has no accounts")
    void testShowLeaderboardEmptyAccountManager() {
        mockAccountManager.accounts().clear();
        assertDoesNotThrow(Leaderboard::showLeaderboard);
    }

    @Test
    @DisplayName("Should sort multiple users correctly by score")
    void testLeaderboardSortingMultipleUsers() {
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
        assertTrue(output.contains("high"));
    }

    @Test
    @DisplayName("Should safely skip entries with missing fields")
    void testMissingFieldsSkipped() {
        JSONObject incomplete = new JSONObject();
        JSONObject user = new JSONObject();
        user.put("username", "broken");
        incomplete.put(UUID.randomUUID().toString(), user);

        mockAccountManager.accounts().putAll(incomplete);

        assertDoesNotThrow(Leaderboard::showLeaderboard);
        assertTrue(outContent.toString().contains("Top 5 Leaderboard"));
    }

    @Test
    @DisplayName("Should handle null totalScore without throwing")
    void testNullTotalScoreHandled() {
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

    @Test
    @DisplayName("Should return correctly ordered leaderboard list")
    void testGetLeaderboardOrdering() throws Exception {
        mockAccountManager.accounts().clear();

        JSONObject multiple = new JSONObject();
        multiple.putAll(buildAccountJson("steve", "TRIVIAL", 120L, 100L));
        multiple.putAll(buildAccountJson("joe", "SUBSTANTIAL", 150L, 300L));
        multiple.putAll(buildAccountJson("frank", "VIRTUOSIC", 200L, 500L));
        mockAccountManager.accounts().putAll(multiple);

        List<String> flat = Leaderboard.getLeaderboard();
        assertEquals(16, flat.size());
        assertEquals("frank", flat.get(2));
    }

    @Test
    @DisplayName("Should include current session even when stored accounts are empty")
    void testGetLeaderboardIncludesCurrentSessionOnly() throws Exception {
        mockAccountManager.accounts().clear();

        injectPrivateField(GameState.class, "time", mockGameState, Duration.ofSeconds(95));
        injectPrivateField(GameState.class, "difficulty", mockGameState, Difficulty.SUBSTANTIAL);

        List<String> flat = Leaderboard.getLeaderboard();
        assertFalse(flat.isEmpty());
        assertEquals("dummy", flat.get(2));
    }

    @Test
    @DisplayName("Should skip leaderboard entries with non-positive remaining time")
    void testGetLeaderboardSkipsZeroTime() {
        mockAccountManager.accounts().clear();

        JSONObject bad = new JSONObject();
        JSONObject hs = new JSONObject();
        hs.put("difficulty", "TRIVIAL");
        hs.put("timeRemaining", 0L);
        hs.put("totalScore", 300L);

        JSONObject acct = new JSONObject();
        acct.put("username", "zeroTime");
        acct.put("highScore", hs);

        bad.put(UUID.randomUUID().toString(), acct);
        mockAccountManager.accounts().putAll(bad);

        List<String> flat = Leaderboard.getLeaderboard();
        assertFalse(flat.contains("zeroTime"));
    }

    @Test
    @DisplayName("Should not throw when highScore exists but totalScore is null")
    void testRecordSessionNullHighScoreDoesNotThrow() throws Exception {
        Score brokenScore = new Score(Duration.ofSeconds(100), Difficulty.TRIVIAL, null);
        injectPrivateField(Account.class, "highScore", mockAccount, brokenScore);
        assertDoesNotThrow(() -> Leaderboard.recordSession("dummy"));
    }

    @Test
    @DisplayName("Should not include entries with negative remaining time in printed leaderboard")
    void testShowLeaderboardSkipsNegativeTime() {
        mockAccountManager.accounts().clear();

        JSONObject bad = new JSONObject();
        JSONObject hs = new JSONObject();
        hs.put("difficulty", "SUBSTANTIAL");
        hs.put("timeRemaining", -50L);
        hs.put("totalScore", 900L);

        JSONObject acct = new JSONObject();
        acct.put("username", "negativeTime");
        acct.put("highScore", hs);

        mockAccountManager.accounts().put(UUID.randomUUID().toString(), acct);

        Leaderboard.showLeaderboard();
        assertFalse(outContent.toString().contains("negativeTime"));
    }

    @Test
    @DisplayName("Should limit leaderboard to top 10 entries")
    void testGetLeaderboardLimitTop10() {
        mockAccountManager.accounts().clear();

        for (int i = 1; i <= 20; i++) {
            JSONObject entry = buildAccountJson("user" + i, "TRIVIAL", 100L + i, (long) i);
            mockAccountManager.accounts().putAll(entry);
        }

        List<String> flat = Leaderboard.getLeaderboard();
        assertEquals(10, flat.size() / 4);
    }

    @Test
    @DisplayName("Should sort by score then difficulty when scores tie")
    void testGetLeaderboardDifficultySorting() {
        mockAccountManager.accounts().clear();

        JSONObject a = buildAccountJson("easy", "TRIVIAL", 100L, 500L);
        JSONObject b = buildAccountJson("hard", "VIRTUOSIC", 100L, 500L);

        mockAccountManager.accounts().putAll(a);
        mockAccountManager.accounts().putAll(b);

        List<String> flat = Leaderboard.getLeaderboard();
        assertEquals("hard", flat.get(2));
    }

    @Test
    @DisplayName("Should skip entry when highScore is not a JSONObject")
    void testHighScoreNotJSONObjectSkipped() {
        mockAccountManager.accounts().clear();

        JSONObject acct = new JSONObject();
        acct.put("username", "badHS");
        acct.put("highScore", "notJson");
        mockAccountManager.accounts().put(UUID.randomUUID().toString(), acct);

        List<String> result = Leaderboard.getLeaderboard();
        assertFalse(result.contains("badHS"));
    }

    @Test
    @DisplayName("Should skip entry missing timeRemaining")
    void testMissingTimeRemainingSkipped() {
        mockAccountManager.accounts().clear();

        JSONObject hs = new JSONObject();
        hs.put("difficulty", "TRIVIAL");
        hs.put("totalScore", 500L);

        JSONObject acct = new JSONObject();
        acct.put("username", "missingTime");
        acct.put("highScore", hs);

        mockAccountManager.accounts().put(UUID.randomUUID().toString(), acct);

        List<String> flat = Leaderboard.getLeaderboard();
        assertFalse(flat.contains("missingTime"));
    }

    @Test
    @DisplayName("Should skip entry missing totalScore")
    void testMissingTotalScoreSkipped() {
        mockAccountManager.accounts().clear();

        JSONObject hs = new JSONObject();
        hs.put("difficulty", "TRIVIAL");
        hs.put("timeRemaining", 100L);

        JSONObject acct = new JSONObject();
        acct.put("username", "missingScore");
        acct.put("highScore", hs);

        mockAccountManager.accounts().put(UUID.randomUUID().toString(), acct);

        List<String> flat = Leaderboard.getLeaderboard();
        assertFalse(flat.contains("missingScore"));
    }    

}
